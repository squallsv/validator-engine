package com.scorpio.validator.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ValidatorException extends RuntimeException {

    private List<Message> messages = new ArrayList<Message>();
    
    public ValidatorException(List<Message> messages) {
        this.messages = messages;
    }
    
    public ValidatorException(Message message) {
        this(Arrays.asList(message));
    }
    
    public List<Message> getMessages() {
        return messages;
    }
}
