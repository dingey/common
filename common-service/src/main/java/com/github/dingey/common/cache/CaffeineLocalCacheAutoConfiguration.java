package com.github.dingey.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(Cache.class)
class CaffeineLocalCacheAutoConfiguration {
    @Autowired
    private Cache<String, Object> cache;

    @Bean
    public LocalCache<String, Object> caffeineCache() {
        return new CaffeineLocalCache(cache);
    }
}
