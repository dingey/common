package com.github.dingey.common;

import io.swagger.annotations.ApiModelProperty;

@SuppressWarnings({"unused"})
public class Result<T> {
    @ApiModelProperty("0成功|1失败|2服务器异常")
    private int code;
    private String msg;
    private T data;

    public Result() {
        super();
    }

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.DefaultResultCode.OK.getCode(), null, data);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.DefaultResultCode.SUCCESS.getCode(), null, data);
    }

    public static <T> Result<T> fail(String msg) {
        return new Result<>(ResultCode.DefaultResultCode.FAIL.getCode(), msg, null);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(ResultCode.DefaultResultCode.ERROR.getCode(), msg, null);
    }

    public static <T> Result<T> code(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> code(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public int getCode() {
        return code;
    }

    public Result<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public Result<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public T getData() {
        return data;
    }

    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }
}