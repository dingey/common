package com.github.dingey.common.lock;

/**
 * 分布式锁
 *
 * @author d
 */
@SuppressWarnings("unused")
public interface DistributedLock {
    /**
     * 获取锁
     *
     * @param lockKey 锁的key
     */
    void lock(String lockKey);

    /**
     * 获取锁
     *
     * @param lockKey   锁的key
     * @param lockValue 锁的value
     */
    void lock(String lockKey, String lockValue);

    /**
     * 仅在调用时释放锁时才获取锁
     *
     * @param lockKey 锁的key
     * @param time    等待锁的最长时间（毫秒）
     * @return {@code true}（如果已获得锁） {@code false}否则
     */
    boolean tryLock(String lockKey, long time);

    /**
     * 仅在调用时释放锁时才获取锁
     *
     * @param lockKey   锁的key
     * @param lockValue 锁的value
     * @param time      等待锁的最长时间（毫秒）
     * @return {@code true}（如果已获得锁） {@code false}否则
     */
    boolean tryLock(String lockKey, String lockValue, long time);

    /**
     * @param lockKey 锁的key
     */
    void unlock(String lockKey);

    /**
     * 释放锁
     *
     * @param lockKey   锁的key
     * @param lockValue 锁的value
     */
    void unlock(String lockKey, String lockValue);
}
