package com.github.dingey.common.service;

import com.github.dingey.mybatis.mapper.BaseMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "unused"})
public abstract class BaseServiceImpl<D extends BaseMapper<T>, T, S extends BaseService> extends AbstractServiceImpl<D, T> implements BaseService<T, S> {
    @Autowired
    private S service;

    @Override
    public List<T> listByIdsCache(Iterable<Serializable> ids) {
        if (ids == null) {
            return Collections.emptyList();
        }
        List<T> list = new ArrayList<>();
        for (Serializable id : ids) {
            T t = (T) service.getCache(id);
            if (t != null) {
                list.add(t);
            }
        }
        return list;
    }

    /**
     * 获取自身的代理对象,内部调用时解决缓存失效问题。
     *
     * @return 代理对象
     */
    public S self() {
        return (S) service;
    }
}
