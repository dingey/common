package com.github.dingey.common.service;

import com.github.dingey.mybatis.mapper.BaseMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageSerializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unused"})
public abstract class AbstractServiceImpl<D extends BaseMapper<T>, T> implements AbstractService<T> {
    @Autowired
    protected D mapper;

    @Override
    public T get(Serializable id) {
        return mapper.get(id);
    }

    @Cacheable(cacheNames = "common", key = "#root.targetClass.simpleName+':'+#id")//, unless = "#result == null")
    @Override
    public T getCache(Serializable id) {
        return mapper.get(id);
    }

    public T get(T t) {
        List<T> list = list(t);
        if (list == null || list.isEmpty()) {
            return null;
        } else if (list.size() == 1) {
            return list.get(0);
        } else {
            throw new RuntimeException("期望1条，但是返回了" + list.size() + "条记录。");
        }
    }

    @Override
    public List<T> list(T entity) {
        return mapper.list(entity);
    }

    @Override
    public Integer count(T entity) {
        return mapper.count(entity);
    }


    @Override
    public List<T> listAll() {
        return mapper.listAll();
    }

    @Override
    public List<T> listByIds(Iterable<Serializable> ids) {
        if (ids == null) {
            return Collections.emptyList();
        }
        return mapper.listByIds(ids);
    }

    @Override
    public int countAll() {
        return mapper.countAll();
    }

    @Override
    public int save(T entity) {
        return mapper.insertSelective(entity);
    }

    @Override
    public int update(T entity) {
        return mapper.updateSelective(entity);
    }

    @CacheEvict(cacheNames = "base", key = "#root.targetClass.simpleName+':'+#entity.id", condition = "#entity.id>0 && #result>0")
    @Override
    public int updateCache(T entity) {
        return mapper.updateSelective(entity);
    }

    @Override
    public int delete(Serializable id) {
        return mapper.delete(id);
    }

    @CacheEvict(cacheNames = "base", key = "#root.targetClass.simpleName+':'+#entity.id", condition = "#id>0 && #result>0")
    @Override
    public int deleteCache(Serializable id) {
        return 0;
    }

    @Override
    public PageSerializable<T> page(T t, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageSerializable<>(mapper.list(t));
    }
}