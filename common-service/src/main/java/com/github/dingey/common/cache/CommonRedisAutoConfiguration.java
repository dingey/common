package com.github.dingey.common.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@ConditionalOnClass(StringRedisTemplate.class)
@AutoConfigureAfter(StringRedisTemplate.class)
public class CommonRedisAutoConfiguration {
    @Autowired
    private StringRedisTemplate srt;
    @Autowired
    private LocalCache localCache;

    @Bean
    public RedisCacheAspect redisCacheAspect() {
        return new RedisCacheAspect(srt, localCache);
    }

    @Bean
    public RedisLockAspect redisLockAspect() {
        return new RedisLockAspect(srt);
    }
}
