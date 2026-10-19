package com.oimc.aimin.base.exception;

import lombok.*;

import java.io.Serial;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private String message;

    public static BusinessException of(String message) {
        return new BusinessException(message);
    }
    public  BusinessException (String message) {
        this.message = message;
    }

}
