package com.github.dingey.common.exception;

import com.github.dingey.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author d
 */
@Order(0)
@RestControllerAdvice
public class CommonExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(CommonExceptionHandler.class);

    @ExceptionHandler(CommonException.class)
    public Result<?> handler(CommonException e) {
        log.debug(e.getMessage(), e);
        return Result.build(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handler(BindException e) {
        log.debug(e.getMessage(), e);
        String collect = e.getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(";"));
        Set<String> data = null;
        if (!CollectionUtils.isEmpty(e.getFieldErrors())) {
            data = e.getFieldErrors().stream().map(FieldError::getField).collect(Collectors.toSet());
        }
        return Result.fail(collect).setData(data);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handler(MethodArgumentNotValidException e) {
        log.debug(e.getMessage(), e);
        String collect = e.getBindingResult().getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(";"));
        Set<String> data = null;
        if (!CollectionUtils.isEmpty(e.getBindingResult().getFieldErrors())) {
            data = e.getBindingResult().getFieldErrors().stream().map(FieldError::getField).collect(Collectors.toSet());
        }
        return Result.fail(collect).setData(data);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handler(MissingServletRequestParameterException e) {
        log.debug(e.getMessage(), e);
        return Result.fail(e.getMessage()).setData(e.getParameterName());
    }
}
