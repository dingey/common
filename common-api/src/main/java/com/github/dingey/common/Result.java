package com.github.dingey.common;

import com.github.dingey.common.exception.CommonException;
import io.swagger.annotations.ApiModelProperty;

import java.beans.Transient;

@SuppressWarnings({"unused"})
public class Result<T> {
    @ApiModelProperty("0成功|1失败|401未授权|403拒绝访问|500服务器异常")
    private int code;
    private String message;
    private T data;

    public Result() {
        super();
    }

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
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

    public static <T> Result<T> build(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> build(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public int getCode() {
        return code;
    }

    public Result<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Result<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getData() {
        return data;
    }

    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }

    /**
     * 判断结果是否成功
     *
     * @return true成功|false失败
     */
    @Transient
    public boolean isSuccess() {
        return 0 == code;
    }

    /**
     * 断言结果是否成功，不成功会抛异常
     */
    @Transient
    public void assertSuccess() {
        if (!isSuccess()) {
            throw new CommonException(this.getMessage()).setCode(this.getCode());
        }
    }
}