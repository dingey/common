package com.github.dingey.common.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("unused")
class DefaultLocalCache implements LocalCache<String, Object> {
    private LocalCacheProperties cacheProperties;
    private final Logger log = LoggerFactory.getLogger(DefaultLocalCache.class);
    private ConcurrentHashMap<String, CacheEntity> cacheMap = new ConcurrentHashMap<>();
    private Executor executor;
    private long lastEvictTime = 0L;
    private Lock lock = new ReentrantLock();

    DefaultLocalCache(LocalCacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
        if (cacheProperties.isEvictExpireTiming() && cacheProperties.isEvictExpireIfFull()) {
            this.executor = Executors.newSingleThreadExecutor();
        }
    }

    DefaultLocalCache(LocalCacheProperties cacheProperties, Executor executor) {
        this.cacheProperties = cacheProperties;
        this.executor = executor;
    }

    @Override
    public boolean hasKey(String key) {
        return cacheMap.containsKey(key);
    }

    @Override
    public Object get(String key) {
        CacheEntity entity = cacheMap.get(key);
        log.debug("获取本地缓存{}", key);
        return entity == null ? null : entity.value;
    }

    @Override
    public void delete(String key) {
        log.debug("删除本地缓存{}", key);
        cacheMap.remove(key);
    }

    void startEvictExpire() {
        new Thread(() -> {
            while (cacheProperties.isEvictExpireTiming()) {
                evictExpire();
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ignore) {
                }
            }
        }).start();
    }

    private void evictExpire() {
        long currentTimeMillis = System.currentTimeMillis();
        for (String k : cacheMap.keySet()) {
            CacheEntity entity = cacheMap.get(k);
            if (entity.expire > currentTimeMillis) {
                continue;
            }
            cacheMap.remove(k);
            log.debug("清除本地过期缓存：{}", k);
        }
        lastEvictTime = currentTimeMillis;
    }

    private void evictExpireIfNeed() {
        if (System.currentTimeMillis() - lastEvictTime > cacheProperties.getEvictInterval()) {
            if (lock.tryLock()) {
                if (cacheProperties.isAsyncEvictExpire()) {
                    executor.execute(this::evictExpire);
                } else {
                    this.evictExpire();
                }
            }
        }
    }

    private boolean needAsyncEvict() {
        return !cacheProperties.isEvictExpireTiming() && cacheProperties.isEvictExpireIfFull() && cacheMap.size() >= cacheProperties.getMaxCapacity();
    }

    @Override
    public void set(String key, Object value, long expire) {
        if (needAsyncEvict()) {
            evictExpireIfNeed();
            return;
        }
        cacheMap.put(key, new CacheEntity(value, 1000L * expire + System.currentTimeMillis()));
    }

    @Override
    public void set(String key, Object value) {
        if (needAsyncEvict()) {
            evictExpireIfNeed();
            return;
        }
        set(key, value, cacheProperties.getExpire());
    }

    static class CacheEntity {
        Object value;
        long expire;

        CacheEntity(Object value, long expire) {
            this.value = value;
            this.expire = expire;
        }
    }
}
