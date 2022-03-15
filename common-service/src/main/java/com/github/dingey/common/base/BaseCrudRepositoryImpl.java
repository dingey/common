package com.github.dingey.common.base;

import com.github.dingey.mybatis.mapper.BaseMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public abstract class BaseCrudRepositoryImpl<M extends BaseMapper<T>, T> implements BaseCrudRepository<T> {
    @Autowired
    protected M mapper;

    @Override
    public T get(Serializable id) {
        return mapper.getById(id);
    }

    @Override
    public T get(T entity) {
        return mapper.get(entity);
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

    @Override
    public int delete(Serializable id) {
        return mapper.deleteById(id);
    }

    public M getMapper() {
        return mapper;
    }
}
