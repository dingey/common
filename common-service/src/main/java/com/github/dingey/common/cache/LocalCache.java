package com.github.dingey.common.cache;

public interface LocalCache<K, V> {
    boolean hasKey(K key);

    V get(K key);

    void delete(K key);

    /**
     * @param key    key
     * @param value  value
     * @param expire 过期时间秒
     */
    void set(K key, V value, long expire);

    void set(K key, V value);
}
