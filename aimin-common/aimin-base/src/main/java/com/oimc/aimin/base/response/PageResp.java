package com.oimc.aimin.base.response;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class PageResp<L> extends BasePageResp<L>{

    @Serial
    private static final long serialVersionUID = 1L;

    private int currentPage;

    private int pageSize;

    private int totalPage;


    public PageResp() {
    }

    public PageResp(List<L> ls, long total) {
        super(ls, total);

    }

    public static <T> PageResp<T> of(int currentPage,int pageSize,  List<T> list, int total) {
        PageResp<T> pageResponse = new PageResp<>();
        pageResponse.setCurrentPage(currentPage);
        pageResponse.setPageSize(pageSize);
        pageResponse.setList(list);
        pageResponse.setTotal(total);
        pageResponse.setTotalPage((pageSize + total - 1) / pageSize);
        return pageResponse;
    }

    public static <T, L> PageResp<L> build(IPage<T> page, Class<L> targetClass) {
        if (null == page) {
            return empty();
        }
        return new PageResp<>(BeanUtil.copyToList(page.getRecords(), targetClass), page.getTotal());
    }

    private static <L> PageResp<L> empty() {
        return new PageResp<>(Collections.emptyList(), 0L);
    }
}
