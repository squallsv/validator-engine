package com.scorpio.validator.exception;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Message {

    private String key;
    private List<String> params;

    public Message(String key, String... params) {
        this.key = key;
        if (params != null) {
            this.params = Arrays.asList(params);
        }
    }

    public Message(String key, List<String> params) {
        this.key = key;
        this.params = params;
    }

    public String getKey() {
        return key;
    }

    public List<String> getParams() {
        return params;
    }

    @Override
    public boolean equals(Object obj) {
        boolean equals = false;
        if (obj == this) {
            equals = true;
        } else if (obj instanceof Message) {
            Message object = (Message) obj;
            equals = new EqualsBuilder().append(key, object.key).append(params, object.params).isEquals();
        }
        return equals;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(key).append(params).toHashCode();
    }
}
