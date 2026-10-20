package com.oimc.aimin.auth.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MiniProgramLoginVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String tokenName;
    private String tokenValue;
}
