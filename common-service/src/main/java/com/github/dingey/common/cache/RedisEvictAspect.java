package com.github.dingey.common.cache;

import com.github.dingey.common.annotation.RedisEvict;
import com.github.dingey.common.util.AspectUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

/**
 * redis锁
 */
@Aspect
class RedisEvictAspect {
    private final Logger log = LoggerFactory.getLogger(RedisEvictAspect.class);
    private final StringRedisTemplate srt;
    private final LocalCache<String, Object> cache;

    RedisEvictAspect(StringRedisTemplate srt, LocalCache<String, Object> localCache) {
        this.srt = srt;
        this.cache = localCache;
    }

    @Pointcut(value = "@annotation(redisEvict)", argNames = "redisEvict")
    public void pointcut(RedisEvict redisEvict) {
    }

    @Around(value = "pointcut(redisEvict)", argNames = "pjp,redisEvict")
    public Object around(ProceedingJoinPoint pjp, RedisEvict redisEvict) throws Throwable {
        if (redisEvict.beforeInvocation()) {
            clearCache(pjp, redisEvict);
        }
        Object result = pjp.proceed();
        if (!redisEvict.beforeInvocation()) {
            clearCache(pjp, redisEvict);
        }
        return result;
    }

    private void clearCache(ProceedingJoinPoint pjp, RedisEvict redisEvict) {
        for (String value : redisEvict.value()) {
            if (StringUtils.isEmpty(value)) {
                continue;
            }
            String key = value.contains("#") ? AspectUtil.spel(pjp, value, String.class) : value;
            srt.delete(key);
            log.debug("清除redis缓存数据,key是{}", key);
            if (redisEvict.local() && cache.hasKey(key)) {
                cache.delete(key);
                log.debug("清除本地缓存数据,key是{}", key);
            }
        }
    }
}