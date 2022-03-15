package com.github.dingey.common.base;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public class BaseCrudServiceImpl<R extends BaseCrudRepository<T>, T> implements BaseCrudService<T> {
    @Autowired
    protected R repository;

    @Override
    public T get(Serializable id) {
        return repository.get(id);
    }

    @Override
    public T get(T entity) {
        return repository.get(entity);
    }

    @Override
    public List<T> list(T entity) {
        return repository.list(entity);
    }

    @Override
    public Long count(T entity) {
        return repository.count(entity);
    }

    @Override
    public List<T> listAll() {
        return repository.listAll();
    }

    @Override
    public List<T> listByIds(Iterable<Serializable> ids) {
        return repository.listByIds(ids);
    }

    @Override
    public long countAll() {
        return repository.countAll();
    }

    @Override
    public int save(T entity) {
        return repository.save(entity);
    }

    @Override
    public int update(T entity) {
        return repository.update(entity);
    }

    @Override
    public int delete(Serializable id) {
        return repository.delete(id);
    }

    public BaseCrudRepository<T> getRepository() {
        return repository;
    }
}
