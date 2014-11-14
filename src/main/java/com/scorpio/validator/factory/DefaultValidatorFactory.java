package com.scorpio.validator.factory;

import org.slf4j.LoggerFactory;

import com.scorpio.validator.Validator;

public enum DefaultValidatorFactory implements ValidatorFactory {

    INSTANCE;

    public <T extends Validator<?>> T createValidator(Class<T> clazz) {
        try {
            return (T) clazz.newInstance();
        } catch (Exception e) {
            LoggerFactory.getLogger(DefaultValidatorFactory.class).error(
                    "An error has occurred while creating product validator", e);
        }

        return null;
    }
}
