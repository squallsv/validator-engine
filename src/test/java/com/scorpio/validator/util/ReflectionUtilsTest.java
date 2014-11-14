package com.scorpio.validator.util;

import org.junit.Assert;
import org.junit.Test;

import com.scorpio.validator.PessoaIdosaValidator;
import com.scorpio.validator.Validator;

public class ReflectionUtilsTest {

    @Test
    public void isClassImplementingInterface_Sucess() {
        Assert.assertTrue(ReflectionUtils.isClassImplementingInterface(PessoaIdosaValidator.class, Validator.class));
    }

    @Test
    public void isClassImplementingInterface_Error() {
        Assert.assertFalse(ReflectionUtils.isClassImplementingInterface(PessoaIdosaValidator.class, Cloneable.class));
    }
}
