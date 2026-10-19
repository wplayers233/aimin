package com.oimc.aimin.base.response;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int code;
    private String msg;
    private T data;

    public static final int ERROR_CODE = 500;
    public static final int SUCCESS_CODE = 200;
    public static final String SUCCESS_MSG = "success";

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.code = ERROR_CODE;
        result.msg = message;
        return result;
    }
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = SUCCESS_CODE;
        result.data = data;
        result.msg = SUCCESS_MSG;
        return result;
    }
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = SUCCESS_CODE;
        result.msg = SUCCESS_MSG;
        return result;
    }
}
