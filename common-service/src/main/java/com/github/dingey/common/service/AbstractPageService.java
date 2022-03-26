package com.github.dingey.common.service;

import com.github.pagehelper.PageSerializable;

public interface AbstractPageService<T> extends AbstractService<T> {
    /**
     * 分页查询
     *
     * @param pageNum  页码
     * @param pageSize 大小
     * @return 分页数据
     */
    PageSerializable<T> page(T t, int pageNum, int pageSize);
}
