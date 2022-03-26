package com.github.dingey.common.service;

import com.github.dingey.mybatis.mapper.BaseMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unused"})
public abstract class AbstractServiceImpl<D extends BaseMapper<T>, T> implements AbstractService<T> {
    @Autowired
    protected D mapper;

    @Override
    public T get(Serializable id) {
        return mapper.getById(id);
    }

//    @Cacheable(cacheNames = "common", key = "#root.targetClass.simpleName+':'+#id")//, unless = "#result == null")
//    @Override
//    public T getCache(Serializable id) {
//        return mapper.getById(id);
//    }

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
    public Long count(T entity) {
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
    public long countAll() {
        return mapper.countAll();
    }

    @Override
    public int save(T entity) {
        return mapper.insert(entity);
    }

    @Override
    public int update(T entity) {
        return mapper.updateById(entity);
    }

//    @CacheEvict(cacheNames = "base", key = "#root.targetClass.simpleName+':'+#entity.id", condition = "#entity.id>0 && #result>0")
//    @Override
//    public int updateCache(T entity) {
//        return mapper.updateById(entity);
//    }

    @Override
    public int delete(Serializable id) {
        return mapper.deleteById(id);
    }

//    @CacheEvict(cacheNames = "base", key = "#root.targetClass.simpleName+':'+#id", condition = "#id>0 && #result>0")
//    @Override
//    public int deleteCache(Serializable id) {
//        return mapper.deleteById(id);
//    }

    public D getMapper() {
        return mapper;
    }
}