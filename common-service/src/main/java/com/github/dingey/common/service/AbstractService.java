package com.github.dingey.common.service;

import com.github.pagehelper.PageSerializable;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings({"rawtypes", "unused"})
public interface AbstractService<T> {
    /**
     * 根据主键查询
     *
     * @param id 主键
     * @return 一条记录
     */
    T get(Serializable id);

    /**
     * 根据主键查询并缓存结果
     *
     * @param id 主键
     * @return 一条记录
     */
    T getCache(Serializable id);

    /**
     * 查询一条记录,和entity字段值不为空相等的一条记录，大于1条报错
     *
     * @param entity 查询对象
     * @return 一条记录
     */
    T get(T entity);

    /**
     * 查询和entity字段值不为空相等的多条记录
     *
     * @param entity 查询对象
     * @return 多条记录
     */
    List<T> list(T entity);

    /**
     * 汇总和entity字段值不为空相等的记录数
     *
     * @param entity 查询对象
     * @return 记录数
     */
    Integer count(T entity);

    /**
     * 查询所有
     *
     * @return 所有记录
     */
    List<T> listAll();

    /**
     * 根据主键批量查询
     *
     * @param ids 主键
     * @return 多条记录
     */
    List<T> listByIds(Iterable<Serializable> ids);

    /**
     * 查询总记录数
     *
     * @return 总记录数
     */
    int countAll();

    /**
     * 插入记录
     *
     * @param entity 实体
     * @return 影响行数
     */
    int save(T entity);

    /**
     * 修改记录
     *
     * @param entity 实体
     * @return 影响的行数
     */
    int update(T entity);

    /**
     * 修改记录,并清除缓存
     *
     * @param entity 实体
     * @return 影响的行数
     */
    int updateCache(T entity);

    /**
     * 删除记录
     *
     * @param id 主键
     * @return 影响的行数
     */
    int delete(Serializable id);

    /**
     * 删除记录并清空缓存
     *
     * @param id 主键
     * @return 影响的行数
     */
    int deleteCache(Serializable id);

    /**
     * 分页查询
     *
     * @param pageNum  页码
     * @param pageSize 大小
     * @return 分页数据
     */
    PageSerializable<T> page(T t, int pageNum, int pageSize);
}
