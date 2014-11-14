package com.scorpio.validator;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslator;

public class ValidatorEvent {

    private Validator validator;
    private final Logger logger = Logger.getLogger(ValidatorEvent.class);

    public static final EventFactory<ValidatorEvent> EVENT_FACTORY = new EventFactory<ValidatorEvent>() {

        public ValidatorEvent newInstance() {
            return new ValidatorEvent();
        }
    };

    public final EventTranslator<ValidatorEvent> translator = new EventTranslator<ValidatorEvent>() {

        public void translateTo(ValidatorEvent event, long sequence) {
            event.setValidator(validator);
            logger.debug("Event translated with sequence " + sequence);
        }
    };

    public ValidatorEvent() {
    }

    public ValidatorEvent(Validator validator) {
        this.validator = validator;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    public Validator getValidator() {
        return validator;
    }

    public EventTranslator<ValidatorEvent> getTranslator() {
        return translator;
    }

    @Override
    public boolean equals(Object obj) {
        boolean equals = false;
        if (obj == this) {
            equals = true;
        } else if (obj instanceof ValidatorEvent) {
            ValidatorEvent object = (ValidatorEvent) obj;
            equals = new EqualsBuilder().append(validator, object.validator).isEquals();
        }
        return equals;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(validator).toHashCode();
    }

}
