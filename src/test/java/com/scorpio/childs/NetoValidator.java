package com.scorpio.childs;

import java.util.List;
import java.util.Map;

import com.scorpio.model.Pessoa;
import com.scorpio.validator.PessoaValidator;
import com.scorpio.validator.annotation.ValidatorConfig;
import com.scorpio.validator.exception.Message;

@ValidatorConfig(type = Pessoa.class)
public class NetoValidator extends PessoaValidator {

    public List<Message> validate(Pessoa entity, Map<String, Object> params) {
        return null;
    }

}
