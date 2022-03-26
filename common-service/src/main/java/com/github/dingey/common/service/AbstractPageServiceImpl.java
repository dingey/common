package com.github.dingey.common.service;

import com.github.dingey.mybatis.mapper.BaseMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageSerializable;

@SuppressWarnings({"unused"})
public abstract class AbstractPageServiceImpl<D extends BaseMapper<T>, T> extends AbstractServiceImpl<D, T> implements AbstractPageService<T> {

    @Override
    public PageSerializable<T> page(T t, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageSerializable<>(mapper.list(t));
    }
}