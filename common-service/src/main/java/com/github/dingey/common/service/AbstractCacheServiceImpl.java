package com.github.dingey.common.service;

import com.github.dingey.mybatis.mapper.BaseMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.io.Serializable;

public class AbstractCacheServiceImpl<D extends BaseMapper<T>, T> extends AbstractServiceImpl<D, T> {
    @Cacheable(cacheNames = "common", key = "#root.targetClass.simpleName+':'+#id")//, unless = "#result == null")
    @Override
    public T get(Serializable id) {
        return super.get(id);
    }

    @CacheEvict(cacheNames = "base", key = "#root.targetClass.simpleName+':'+#entity.id", condition = "#entity.id>0 && #result>0")
    @Override
    public int update(T entity) {
        return super.update(entity);
    }

    @CacheEvict(cacheNames = "base", key = "#root.targetClass.simpleName+':'+#id", condition = "#id>0 && #result>0")
    @Override
    public int delete(Serializable id) {
        return super.delete(id);
    }
}
