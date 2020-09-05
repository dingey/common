package com.github.dingey.common.lock;

public class ZookeeperDistributedLock implements DistributedLock {
    @Override
    public void lock(String lockKey) {

    }

    @Override
    public void lock(String lockKey, String lockValue) {

    }

    @Override
    public boolean tryLock(String lockKey, long time) {
        return false;
    }

    @Override
    public boolean tryLock(String lockKey, String lockValue, long time) {
        return false;
    }

    @Override
    public void unlock(String lockKey) {

    }

    @Override
    public void unlock(String lockKey, String lockValue) {

    }
}
