package com.github.dingey.common;

public interface ResultCode {
    int getCode();

    String getMessage();

    enum DefaultResultCode implements ResultCode {
        OK(0, "ok"),
        SUCCESS(0, "success"),
        FAIL(1, "fail"),
        UNAUTHORIZED(401, "unauthorized"),
        FORBIDDEN(403, "forbidden"),
        ERROR(500, "error"),
        ;
        int code;
        String message;

        DefaultResultCode(int code, String message) {
            this.code = code;
            this.message = message;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }
}
