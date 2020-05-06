package com.github.dingey.common.exception;

public class RedisLockException extends RuntimeException {

    public RedisLockException(String message) {
        super(message);
    }

}
