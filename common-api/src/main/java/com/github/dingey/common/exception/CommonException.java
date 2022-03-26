package com.github.dingey.common.exception;

import com.github.dingey.common.ResultCode;

/**
 * @author d
 */
public class CommonException extends RuntimeException {
    private int code = ResultCode.DefaultResultCode.FAIL.getCode();

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

    public CommonException(ResultCode resultCode) {
        super(resultCode.getMessage());
        setCode(resultCode.getCode());
    }

    public int getCode() {
        return code;
    }

    public CommonException setCode(int code) {
        this.code = code;
        return this;
    }
}
