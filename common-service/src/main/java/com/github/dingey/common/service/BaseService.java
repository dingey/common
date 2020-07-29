package com.github.dingey.common.service;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings({"rawtypes", "unused"})
public interface BaseService<T, S extends BaseService> extends AbstractService<T> {
    /**
     * 根据主键从缓存查询
     *
     * @param ids 主键
     * @return 多条记录
     */
    List<T> listByIdsCache(Iterable<Serializable> ids);
}