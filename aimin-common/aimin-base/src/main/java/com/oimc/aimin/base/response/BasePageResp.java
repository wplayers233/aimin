package com.oimc.aimin.base.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BasePageResp<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<T> list;

    private long total;

    public BasePageResp() {
    }

    public BasePageResp(final List<T> list, final long total) {
        this.list = list;
        this.total = total;
    }
}
