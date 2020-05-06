package com.github.dingey.common.exception;

public class JsonException extends RuntimeException {

    public JsonException(String message) {
        super(message);
    }

    public JsonException(Throwable cause) {
        super(cause);
    }

}
