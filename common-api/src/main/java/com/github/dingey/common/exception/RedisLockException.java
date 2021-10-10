package com.github.dingey.common.exception;

public class RedisLockException extends CommonException {

    public RedisLockException(String message) {
        super(message);
    }

    public RedisLockException(Throwable cause) {
        super(cause);
    }
}
