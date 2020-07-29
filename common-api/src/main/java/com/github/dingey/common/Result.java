package com.github.dingey.common;

@SuppressWarnings({"unused", "unchecked"})
public class Result<T> {
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

    public static <T> Result success(T data) {
        return new Result(0, null, data);
    }

    public int getCode() {
        return code;
    }

    public Result setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public Result setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public T getData() {
        return data;
    }

    public Result setData(T data) {
        this.data = data;
        return this;
    }
}