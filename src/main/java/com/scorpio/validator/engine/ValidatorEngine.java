package com.scorpio.validator.engine;

import java.util.Map;

import com.scorpio.validator.exception.ValidatorException;
import com.scorpio.validator.factory.ValidatorFactory;

/**
 * <p>
 * A Validator Engine implementation is a class that searches for validators
 * classes and executes their validations.
 * </p>
 * 
 * @author cabl
 * 
 */
public interface ValidatorEngine {

    /**
     * This method sets in motion a sequence of events that triggers validations
     * around the application accordingly to the type of class passed by.
     * 
     * @param entity the entity that the test is based of.
     * @param params the parameters to pass to the validations.
     * @param scanPackages the packages to scan for validators.
     * 
     * @throws ValidatorException if any validation results in messages, this
     *         exception will be throwed.
     */
    <T> void execute(T entity, Map<String, Object> params, String... scanPackages) throws ValidatorException;

    /**
     * This method sets in motion a sequence of events that triggers validations
     * around the application accordingly to the type of class passed by.
     * 
     * @param entity the entity that the test is based of.
     * @param params the parameters to pass to the validations.
     * @param factory a custom ValidatorFactory to create the validators.
     * @param scanPackages the packages to scan for validators.
     * 
     * @throws ValidatorException if any validation results in messages, this
     *         exception will be throwed.
     */
    <T> void execute(T entity, Map<String, Object> params, ValidatorFactory factory, String... scanPackages)
            throws ValidatorException;

}
