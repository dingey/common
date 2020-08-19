package com.github.dingey.common.exception;

/**
 * @author d
 */
public class CommonException extends RuntimeException {
    public CommonException() {
    }

    public CommonException(String message) {
        super(message);
    }

    public CommonException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommonException(Throwable cause) {
        super(cause);
    }
}
