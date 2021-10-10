package com.github.dingey.common.exception;

import com.github.dingey.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}
