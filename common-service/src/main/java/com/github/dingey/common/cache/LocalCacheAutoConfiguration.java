package com.github.dingey.common.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

@Configuration
@EnableConfigurationProperties(LocalCacheProperties.class)
@ConditionalOnMissingClass("com.github.benmanes.caffeine.cache.Cache")
class LocalCacheAutoConfiguration {
    @Autowired
    private LocalCacheProperties localCacheProperties;
    @Autowired(required = false)
    private Executor executor;

    @Bean
    public LocalCache<String, Object> localCache() {
        DefaultLocalCache defaultLocalCache;
        if (executor == null) {
            defaultLocalCache = new DefaultLocalCache(localCacheProperties);
        } else {
            defaultLocalCache = new DefaultLocalCache(localCacheProperties, executor);
        }
        if (localCacheProperties.isEvictExpireTiming()) {
            defaultLocalCache.startEvictExpire();
        }
        return defaultLocalCache;
    }

}
