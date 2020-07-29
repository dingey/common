package com.github.dingey.common.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties(prefix = "common.cache.local")
public class LocalCacheProperties {
    /**
     * 默认过期时间：秒
     */
    private long expire = 60L;
    /**
     * 缓存数据大小
     */
    private int maxCapacity = Byte.MAX_VALUE;
    /**
     * 是否启动单独线程定时清除过期数据
     */
    private boolean evictExpireTiming = false;
    /**
     * 缓存上限时是否启动清除过期
     */
    private boolean evictExpireIfFull = false;
    /**
     * 缓存上限时是否异步清除过期
     */
    private boolean asyncEvictExpire = false;
    /**
     * 清除过期数据间隔时间:毫秒
     */
    private long evictInterval = 1000L;

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public boolean isEvictExpireTiming() {
        return evictExpireTiming;
    }

    public void setEvictExpireTiming(boolean evictExpireTiming) {
        this.evictExpireTiming = evictExpireTiming;
    }

    public boolean isEvictExpireIfFull() {
        return evictExpireIfFull;
    }

    public void setEvictExpireIfFull(boolean evictExpireIfFull) {
        this.evictExpireIfFull = evictExpireIfFull;
    }

    public long getEvictInterval() {
        return evictInterval;
    }

    public void setEvictInterval(long evictInterval) {
        this.evictInterval = evictInterval;
    }

    public boolean isAsyncEvictExpire() {
        return asyncEvictExpire;
    }

    public void setAsyncEvictExpire(boolean asyncEvictExpire) {
        this.asyncEvictExpire = asyncEvictExpire;
    }
}
