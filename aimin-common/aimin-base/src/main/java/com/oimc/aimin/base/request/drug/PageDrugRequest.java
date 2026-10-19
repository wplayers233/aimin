package com.oimc.aimin.base.request.drug;

import com.oimc.aimin.base.request.BasePageRequest;
import lombok.Data;

/*
 *
 * @author 渣哥
 */
@Data
public class PageDrugRequest extends BasePageRequest {

    private String content;
    private Integer categoryId;
}
