package com.github.dingey.common.lock;

import com.github.dingey.common.exception.RedisLockException;

public interface CommonLock {
    /**
     * 获取锁，阻塞到获取锁成功或者线程被中断时抛出异常
     *
     * @param lockKey 锁的key
     */
    void lock(String lockKey) throws RedisLockException;

    /**
     * 获取锁，阻塞到获取锁成功或者线程被中断时抛出异常
     *
     * @param lockKey   锁的key
     * @param lockValue 锁的value
     */
    void lock(String lockKey, String lockValue) throws RedisLockException;

    /**
     * 获取锁
     *
     * @param lockKey 锁的key
     * @param timeout 等待锁的最长时间（毫秒）
     * @return {@code true}（如果已获得锁） {@code false}否则
     */
    boolean tryLock(String lockKey, long timeout) throws RedisLockException;

    /**
     * 仅在调用时释放锁时才获取锁
     *
     * @param lockKey   锁的key
     * @param lockValue 锁的value
     * @param timeout   等待锁的最长时间（毫秒）
     * @return {@code true}（如果已获得锁） {@code false}否则
     */
    boolean tryLock(String lockKey, String lockValue, long timeout) throws RedisLockException;

    /**
     * @param lockKey 锁的key
     */
    void unlock(String lockKey) throws RedisLockException;

    /**
     * 释放锁
     *
     * @param lockKey   锁的key
     * @param lockValue 锁的value
     */
    void unlock(String lockKey, String lockValue) throws RedisLockException;
}
