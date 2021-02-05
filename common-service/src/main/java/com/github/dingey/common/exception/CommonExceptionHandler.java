package com.github.dingey.common.exception;

import com.github.dingey.common.Result;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author d
 */
@Order(0)
@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(CommonException.class)
    public Result<?> handler(CommonException e) {
        return Result.error(e.getMessage());
    }
}
