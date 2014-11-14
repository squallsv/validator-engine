package com.scorpio.validator.engine;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.lmax.disruptor.NoOpEventProcessor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.scorpio.validator.Validator;
import com.scorpio.validator.ValidatorEvent;
import com.scorpio.validator.annotation.ValidatorConfig;
import com.scorpio.validator.exception.InfrastructureException;
import com.scorpio.validator.exception.Message;
import com.scorpio.validator.exception.ValidatorException;
import com.scorpio.validator.factory.DefaultValidatorFactory;
import com.scorpio.validator.factory.ValidatorFactory;
import com.scorpio.validator.util.ReflectionUtils;

public class DefaultValidatorEngine implements ValidatorEngine {

    private ExecutorService executor = Executors.newFixedThreadPool(2, DaemonThreadFactory.INSTANCE);
    private final Logger logger = Logger.getLogger(DefaultValidatorEngine.class);

    public <T> void execute(T entity, java.util.Map<String, Object> params, ValidatorFactory factory,
            String... scanPackages) throws ValidatorException {
        List<Message> errors = new ArrayList<Message>();

        logger.debug("Starting execution!");

        logger.debug("Scanning validators!");

        List<Class<Validator<T>>> validatorsClasses = scan(entity.getClass(), scanPackages);

        if (!validatorsClasses.isEmpty()) {
            List<Callable<String>> producers = new ArrayList<Callable<String>>();

            logger.debug("Creating product validators buffer!");
            ProducerCallable<Validator<T>> validatorProducer =
                    new ProducerCallable<Validator<T>>(validatorsClasses, factory);

            producers.add(validatorProducer);

            logger.debug("Initiating production of the validators!");
            try {
                executor.invokeAll(producers);
            } catch (Exception e) {
                logger.error("An error has occurred while producing the validators", e);
                throw new InfrastructureException(e);
            }

            List<Callable<List<Message>>> consumers = new LinkedList<Callable<List<Message>>>();
            ConsumerCallable productConsumer =
                    new ConsumerCallable(entity, params, validatorProducer.getBuffer(),
                            validatorProducer.getSequenceBarrier());
            consumers.add(productConsumer);

            logger.debug("Initiating consumption of the validators!");
            try {
                List<Future<List<Message>>> result = executor.invokeAll(consumers);
                for (Future<List<Message>> future : result) {
                    errors.addAll(future.get());
                }
            } catch (Exception e) {
                logger.error("An error has occurred while consuming the validators", e);
                throw new InfrastructureException(e);

            }
        }

        logger.debug("Finalizing the execution of the engine!");

        if (!errors.isEmpty()) {
            throw new ValidatorException(errors);
        }
    }

    public <T> void execute(T entity, Map<String, Object> params, String... scanPackages) throws ValidatorException {
        execute(entity, params, DefaultValidatorFactory.INSTANCE, scanPackages);
    }

    /**
     * Scans packages looking for validators of the type that was passed to this
     * method.
     * 
     * @param type type of the class that the validator is configured to
     *        validate.
     * @param packagesToScan packages to scan.
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends Validator<?>> List<Class<T>> scan(Class<?> type, String... packagesToScan) {
        List<Class<T>> validatorsClasses = new ArrayList<Class<T>>();
        List<Class<T>> superClassValidators = new ArrayList<Class<T>>();

        for (String packageToScan : packagesToScan) {
            if (StringUtils.isBlank(packageToScan)) {
                throw new IllegalArgumentException("Scan package cannot be null or empty!!");
            }
            if (StringUtils.contains(packageToScan, ".")) {
                packageToScan = packageToScan.replaceAll("\\.", "\\/");
            }

            List<String> classesNames = findClassesNames(packageToScan);

            // Find classes with the annotations.
            for (String className : classesNames) {
                Class<T> cls;

                try {
                    cls = (Class<T>) Class.forName(className);
                    if (cls.isAnnotationPresent(ValidatorConfig.class)
                            && ReflectionUtils.isClassImplementingInterface(cls, Validator.class)) {
                        ValidatorConfig annotation = cls.getAnnotation(ValidatorConfig.class);

                        if (annotation.type().equals(type) || annotation.type().equals(type.getSuperclass())) {
                            Class<?> superClass = cls.getSuperclass();
                            if (superClass.isAnnotationPresent(ValidatorConfig.class)) {
                                superClassValidators.add((Class<T>) superClass);
                            }
                            validatorsClasses.add((Class<T>) cls);
                        }
                    }
                } catch (Exception e) {
                    logger.error("An error has occurred during the scanning of the validators!", e);
                    throw new InfrastructureException(e);
                }
            }
        }

        // Turning off parent validator, if you subclass a validator, only the subclass will be executed.
        if (!superClassValidators.isEmpty()) {
            validatorsClasses.removeAll(superClassValidators);
        }

        return validatorsClasses;
    }

    private List<String> findClassesNames(String packageToScan) {
        List<String> files = new ArrayList<String>();
        Queue<File> packages = new LinkedList<File>();

        URL root = Thread.currentThread().getContextClassLoader().getResource(packageToScan);

        if (root != null) {
            File rootDir = new File(root.getFile());

            packages.add(rootDir);
            while (!packages.isEmpty()) {
                File pkg = packages.poll();

                File[] deepPackages = pkg.listFiles();

                File[] classes = pkg.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".class");
                    }
                });

                if (deepPackages != null) {
                    packages.addAll(Arrays.asList(deepPackages));
                }

                if (classes != null) {
                    for (File file : classes) {
                        files.add(getFullClassName(file));
                    }
                }
            }
        }

        return files;
    }

    private String getFullClassName(File file) {
        StringBuilder builder = new StringBuilder();

        List<File> files = new ArrayList<File>();
        files.add(file);

        while (file.getParentFile() != null) {
            file = file.getParentFile();
            files.add(file);
        }

        Collections.reverse(files);

        Iterator<File> iterator = files.iterator();

        while (iterator.hasNext()) {
            String fileName = iterator.next().getName();

            if (StringUtils.isEmpty(fileName)) {
                continue;
            }

            builder.append(fileName);
            if (iterator.hasNext()) {
                builder.append(".");
            }
        }

        String classFullName = builder.toString().replaceAll(".class$", "");
        while (!ReflectionUtils.classExists(classFullName) && StringUtils.isNotBlank(classFullName)) {
            String firstPart = classFullName.substring(0, classFullName.indexOf(".") + 1);
            classFullName = classFullName.replaceFirst(firstPart, "");
        }

        return classFullName;
    }

}

class ProducerCallable<T extends Validator<?>> implements Callable<String> {

    private static final String DONE = "DONE";

    private RingBuffer<ValidatorEvent> buffer;
    private SequenceBarrier sequenceBarrier;

    private List<Class<T>> classes;
    private ValidatorFactory factory;

    public ProducerCallable(List<Class<T>> classes, ValidatorFactory factory) {
        this.classes = classes;
        this.factory = factory;
        createBuffer();
    }

    private final void createBuffer() {
        if (!classes.isEmpty()) {
            buffer = RingBuffer.createMultiProducer(ValidatorEvent.EVENT_FACTORY, classes.size());
            sequenceBarrier = buffer.newBarrier();
            buffer.addGatingSequences(new NoOpEventProcessor(buffer).getSequence());
        }
    }

    public String call() throws Exception {
        for (Class<T> clazz : classes) {
            ValidatorEvent event = new ValidatorEvent(factory.createValidator(clazz));
            buffer.publishEvent(event.getTranslator());
        }

        return DONE;
    }

    public RingBuffer<ValidatorEvent> getBuffer() {
        return buffer;
    }

    public SequenceBarrier getSequenceBarrier() {
        return sequenceBarrier;
    }

}

class ConsumerCallable implements Callable<List<Message>> {

    private Object entity;
    private Map<String, Object> params;
    private RingBuffer<ValidatorEvent> buffer;
    private SequenceBarrier sequenceBarrier;
    private int nextValue = -1;

    public ConsumerCallable(Object entity, Map<String, Object> params, RingBuffer<ValidatorEvent> buffer,
            SequenceBarrier sequenceBarrier) {
        this.buffer = buffer;
        this.sequenceBarrier = sequenceBarrier;
        this.entity = entity;
        this.params = params;
    }

    @SuppressWarnings("unchecked")
    public List<Message> call() throws Exception {
        List<Message> messages = new ArrayList<Message>();

        if (buffer != null) {
            while (nextValue < buffer.getBufferSize() - 1) {
                if (nextValue == sequenceBarrier.getCursor()) {
                    continue;
                }

                long sequence = ++nextValue;

                if (sequence <= sequenceBarrier.getCursor()) {
                    ValidatorEvent event = buffer.get(sequence);
                    messages.addAll(event.getValidator().validate(entity, params));
                }
            }
        }

        return messages;
    }

}
