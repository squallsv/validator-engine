package com.scorpio.validator;

import java.util.List;
import java.util.Map;

import com.scorpio.validator.exception.Message;

/**
 * <p>
 * A Validator implementation is a class that can perform general validations to
 * be consumed on a Validator Engine.
 * </p>
 * 
 * @author cabl
 * 
 */
public interface Validator<T> {

    /**
     * Method that executes the validation and fills a list with the errors.
     * 
     * @param entity entity that is being validated.
     * @param params general parameters that can be passed to the validation.
     * 
     * @return a list of error messages generated on this method.
     */
    List<Message> validate(T entity, Map<String, Object> params);
}
