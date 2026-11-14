package com.campus.market.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 统一分页返回结构：records=当前页数据，total=总条数。
 * 另外带上 page/size/pages，方便前端做无限滚动时判断是否还有下一页。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> records;

    private long total;

    private long page;

    private long size;

    private long pages;

    public static <T> PageResult<T> of(IPage<?> page, List<T> records) {
        return new PageResult<>(
                records,
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getPages()
        );
    }
}
