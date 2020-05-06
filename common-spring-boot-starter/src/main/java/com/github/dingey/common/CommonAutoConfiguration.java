package com.github.dingey.common;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

@Configuration
@ConditionalOnClass(StringRedisTemplate.class)
@AutoConfigureAfter(StringRedisTemplate.class)
public class CommonAutoConfiguration {
    @Resource
    private StringRedisTemplate srt;

    @Bean
    public RedisCacheAspect redisCacheAspect() {
        return new RedisCacheAspect(srt);
    }

    @Bean
    public RedisLockAspect redisLockAspect() {
        return new RedisLockAspect(srt);
    }
}
