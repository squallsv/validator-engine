package com.scorpio.validator.exception;

public class InfrastructureException extends RuntimeException {

    public InfrastructureException(Throwable cause) {
        super(cause);
    }
    
    public InfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InfrastructureException(String message) {
        super(message);
    }

}
