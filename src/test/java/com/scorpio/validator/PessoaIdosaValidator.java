package com.scorpio.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.scorpio.model.Pessoa;
import com.scorpio.validator.annotation.ValidatorConfig;
import com.scorpio.validator.exception.Message;

@ValidatorConfig(type = Pessoa.class)
public class PessoaIdosaValidator implements Validator<Pessoa> {

    public List<Message> validate(Pessoa pessoa, Map<String, Object> params) {
        List<Message> errors = new ArrayList<Message>();

        validationOne(pessoa, errors);

        return errors;
    }

    private void validationOne(Pessoa pessoa, List<Message> messages) {
        messages.add(new Message("error_one_idosa"));
    }
}
