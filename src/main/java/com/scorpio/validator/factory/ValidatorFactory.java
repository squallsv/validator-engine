package com.scorpio.validator.factory;

import com.scorpio.validator.Validator;

/**
 * <p>
 * A ValidatorFactory implementation is a class that creates validators.
 * </p>
 * 
 * @author cabl
 * 
 */
public interface ValidatorFactory {

    /**
     * This method creates a validator based on the type of the class.
     * 
     * @param clazz type of the class.
     * 
     * @return a validator.
     */
    <T extends Validator<?>> T createValidator(Class<T> clazz);
}
