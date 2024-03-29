package com.github.dingey.common.cache;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@ConditionalOnClass({RedisConnectionFactory.class, ProceedingJoinPoint.class})
public class SpringCacheAutoConfiguration {
    @Primary
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
        redisCacheConfiguration = redisCacheConfiguration.entryTtl(Duration.ofMinutes(5L))
                //.disableCachingNullValues() //如果是空值，不缓存
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer((new GenericJackson2JsonRedisSerializer())));
        return RedisCacheManager
                .builder(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
                .cacheDefaults(redisCacheConfiguration).build();
    }

//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        redisTemplate.setKeySerializer(keySerializer());
//        redisTemplate.setHashKeySerializer(keySerializer());
//        redisTemplate.setValueSerializer(valueSerializer());
//        redisTemplate.setHashValueSerializer(valueSerializer());
//        return redisTemplate;
//    }

//    private RedisSerializer<String> keySerializer() {
//        return new StringRedisSerializer();
//    }
//
//    //使用Jackson序列化器
//    private RedisSerializer<Object> valueSerializer() {
//        return new GenericJackson2JsonRedisSerializer();
//    }

    @Bean
    @RefreshScope
    public RedisCacheAspect redisCacheAspect(StringRedisTemplate srt) {
        return new RedisCacheAspect(srt);
    }

    @Bean
    @RefreshScope
    public RedisEvictAspect redisEvictAspect(StringRedisTemplate srt) {
        return new RedisEvictAspect(srt);
    }
}
