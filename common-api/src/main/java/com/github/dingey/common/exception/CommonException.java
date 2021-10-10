package com.github.dingey.common.exception;

import com.github.dingey.common.ResultCode;

/**
 * @author d
 */
public class CommonException extends RuntimeException {
    private int code = ResultCode.DefaultResultCode.ERROR.getCode();

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

    public int getCode() {
        return code;
    }

    public CommonException setCode(int code) {
        this.code = code;
        return this;
    }
}
