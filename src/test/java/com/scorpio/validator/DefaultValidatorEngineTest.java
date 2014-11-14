package com.scorpio.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.junit.Test;

import com.lmax.disruptor.util.DaemonThreadFactory;
import com.scorpio.model.Filho;
import com.scorpio.model.Pessoa;
import com.scorpio.validator.engine.DefaultValidatorEngine;
import com.scorpio.validator.exception.Message;
import com.scorpio.validator.exception.ValidatorException;

public class DefaultValidatorEngineTest {

    @Test
    public void testScan_noValidators() {
        DefaultValidatorEngine validatorEngine = new DefaultValidatorEngine();

        List<Class<Validator<Pessoa>>> validators = validatorEngine.scan(Pessoa.class, "com.scorpio.model");

        Assert.assertTrue(validators.isEmpty());
    }

    @Test
    public void testScan_noValidators_becauseValidatorDoesNotImplementInterface() {
        DefaultValidatorEngine validatorEngine = new DefaultValidatorEngine();

        List<Class<Validator<Pessoa>>> validators = validatorEngine.scan(Pessoa.class, "com/scorpio/model");

        Assert.assertTrue(validators.isEmpty());
    }

    @Test
    public void testScan_TwoValidators() {
        DefaultValidatorEngine validatorEngine = new DefaultValidatorEngine();

        List<Class<Validator<Pessoa>>> validators = validatorEngine.scan(Pessoa.class, "com/scorpio/validator");

        Assert.assertEquals(2, validators.size());
    }

    /**
     * Expected only the two subclasses.
     */
    @Test
    public void testScan_TwoValidatorsWithTwoParentValidators() {
        DefaultValidatorEngine validatorEngine = new DefaultValidatorEngine();

        List<Class<Validator<Pessoa>>> validators =
                validatorEngine.scan(Pessoa.class, "com.scorpio.validator", "com.scorpio.test");

        Assert.assertEquals(2, validators.size());
    }

    @Test
    public void testScan_TwoValidatorsInsideADeepPackage() {
        DefaultValidatorEngine validatorEngine = new DefaultValidatorEngine();

        List<Class<Validator<Pessoa>>> validators = validatorEngine.scan(Pessoa.class, "com.scorpio.deep");

        Assert.assertEquals(2, validators.size());
    }

    @Test
    public void testScan_TwoValidatorsOfTheParentClass() {
        DefaultValidatorEngine validatorEngine = new DefaultValidatorEngine();

        List<Class<Validator<Pessoa>>> validators = validatorEngine.scan(Filho.class, "com.scorpio.deep");

        Assert.assertEquals(2, validators.size());
    }

    /**
     * Scenario 1:
     * 
     * I have two validator class that does 3 different validations, that
     * generates 3 different messages.
     * 
     */
    @Test
    public void testValidate_Scenario1() {
        DefaultValidatorEngine engine = new DefaultValidatorEngine();

        try {
            engine.execute(new Pessoa(), null, "com.scorpio.validator");
            Assert.fail("Should've thrown an exception!");
        } catch (ValidatorException ex) {
            Assert.assertEquals(3, ex.getMessages().size());
        }
    }

    /**
     * Scenario 2:
     * 
     * Same as scenario 1, but with 30 different users. So we are expecting 90
     * messages on total, each thread will result in 3 messages.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     * 
     */
    @Test
    public void testValidate_Scenario2() throws InterruptedException, ExecutionException {
        Callable<ValidatorException> task = new Callable<ValidatorException>() {

            public ValidatorException call() throws Exception {
                DefaultValidatorEngine engine = new DefaultValidatorEngine();
                try {
                    engine.execute(new Pessoa(), null, "com.scorpio.validator");
                } catch (ValidatorException ex) {
                    return ex;
                }

                return null;
            }
        };
        List<Callable<ValidatorException>> tasks = Collections.nCopies(30, task);
        ExecutorService executorService = Executors.newFixedThreadPool(30, DaemonThreadFactory.INSTANCE);

        List<Future<ValidatorException>> futures = executorService.invokeAll(tasks);

        List<Message> resultList = new ArrayList<Message>();

        // Check for exceptions
        for (Future<ValidatorException> future : futures) {
            // Throws an exception if an exception was thrown by the task.
            resultList.addAll(future.get().getMessages());
        }
        // Validate the IDs
        Assert.assertEquals(90, resultList.size());
    }
}
