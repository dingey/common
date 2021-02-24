package com.github.dingey.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Set;

/**
 * @author d
 */
@Component
@ConditionalOnClass(Validator.class)
public class ValidatorUtil {
    private static Validator validator;

    @Autowired
    public void setValidator(Validator validator) {
        ValidatorUtil.validator = validator;
    }

    /**
     * <a href="https://developer.ibm.com/zh/articles/j-lo-jsr303/">手动校验JSR-303</a>
     *
     * @param target 待校验对象
     * @param groups 校验组
     * @throws ConstraintViolationException 校验异常
     */
    public static <T> void validate(T target, Class... groups) throws ConstraintViolationException {
        Set<ConstraintViolation<T>> validateResult = validator.validate(target, groups);
        if (validateResult.size() > 0) {
            throw new ConstraintViolationException(validateResult);
        }
    }
}