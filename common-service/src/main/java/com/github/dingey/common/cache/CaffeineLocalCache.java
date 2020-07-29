package com.github.dingey.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CaffeineLocalCache implements LocalCache<String, Object> {
    private final Logger log = LoggerFactory.getLogger(CaffeineLocalCache.class);
    private Cache<String, Object> cache;

    public CaffeineLocalCache(Cache<String, Object> cache) {
        this.cache = cache;
    }

    @Override
    public boolean hasKey(String key) {
        return cache.getIfPresent(key) != null;
    }

    @Override
    public Object get(String key) {
        log.debug("从本地caffeine缓存获取：{}", key);
        return cache.getIfPresent(key);
    }

    @Override
    public void delete(String key) {
        cache.invalidate(key);
    }

    @Override
    public void set(String key, Object value, long expire) {
        log.debug("缓存本地caffeine缓存：{}", key);
        cache.put(key, value);
    }

    @Override
    public void set(String key, Object value) {
        log.debug("缓存本地caffeine缓存：{}", key);
        cache.put(key, value);
    }
}
