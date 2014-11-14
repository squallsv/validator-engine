package com.scorpio.validator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidatorConfig {

    /**
     * This parameter qualifies the type of class that's going to be validated.
     * 
     * @return the type.
     */
    public Class<?> type();
}
