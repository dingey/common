package com.github.dingey.common;

import com.github.dingey.common.annotation.RedisCache;
import com.github.dingey.common.util.AspectUtil;
import com.github.dingey.common.util.JsonUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * redis锁
 */
@Aspect
@Component
class RedisCacheAspect {
    private final Logger log = LoggerFactory.getLogger(RedisCacheAspect.class);
    private final StringRedisTemplate srt;
    private final static long WAIT_TIMEOUT = 1000L;

    @Autowired
    public RedisCacheAspect(StringRedisTemplate srt) {
        this.srt = srt;
    }

    @Pointcut(value = "@annotation(redisCache)", argNames = "redisCache")
    public void pointcut(RedisCache redisCache) {
    }

    @Around(value = "pointcut(redisCache)", argNames = "pjp,redisCache")
    public Object around(ProceedingJoinPoint pjp, RedisCache redisCache) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        String key = (StringUtils.isEmpty(redisCache.cacheName()) ? redisCache.value() : redisCache.cacheName()) +
                (StringUtils.hasText(redisCache.key()) ? ":" : "") +
                (StringUtils.hasText(redisCache.key()) ? AspectUtil.spel(pjp, redisCache.key(), String.class) : "");
        if (StringUtils.isEmpty(key)) {
            key = String.format("%s:%s:%s", method.getDeclaringClass().getName().replace(".", ":"), method.getName(), JsonUtil.toJson(pjp.getArgs()));
        }
        if (Objects.equals(srt.hasKey(key), true)) {
            return getFromCache(key, method.getReturnType());
        } else {
            Object result;
            if (!redisCache.cacheResult()) {
                return pjp.proceed();
            }
            if (redisCache.sync()) {
                boolean b = setLock(key);
                if (b) {
                    if (Objects.equals(srt.hasKey(key), true)) {
                        return getFromCache(key, method.getReturnType());
                    } else {
                        result = pjp.proceed();
                    }
                } else {
                    return tryWaitUntilCacheed(key, method);
                }
            } else {
                result = pjp.proceed();
            }

            Map<String, Object> args = new HashMap<>();
            args.put("result", result);
            if (StringUtils.isEmpty(redisCache.condition()) || AspectUtil.spel(pjp, redisCache.condition(), boolean.class, args)) {
                if (StringUtils.isEmpty(redisCache.unless()) || !AspectUtil.spel(pjp, redisCache.unless(), boolean.class, args)) {
                    String s = JsonUtil.toJson(result);
                    srt.opsForValue().setIfAbsent(key, s, redisCache.expire(), TimeUnit.SECONDS);
                    log.debug("满足缓存条件，缓存数据,key是{},value:{}", key, s);
                }
            }
            return result;
        }
    }

    private Object tryWaitUntilCacheed(String key, Method method) {
        log.debug("等待其他应用更新缓存，key是{}", key);
        long l = System.currentTimeMillis();
        boolean hasKey = Objects.equals(srt.hasKey(key), true);
        while (!hasKey) {
            try {
                Thread.sleep(10L);
                hasKey = Objects.equals(srt.hasKey(key), true);
                if (System.currentTimeMillis() - l > WAIT_TIMEOUT) {
                    log.debug("等待其他应用更新缓存超时，key是{}", key);
                    return null;
                }
                log.debug("等待其他应用更新缓存，key是{}", key);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                return true;
            }
        }
        return getFromCache(key, method.getReturnType());
    }

    private boolean setLock(String key) {
        return Objects.equals(srt.opsForValue().setIfAbsent("LOCK:" + key, "0", 9000L, TimeUnit.MILLISECONDS), true);
    }

    private Object getFromCache(String key, Class<?> type) {
        String s = srt.opsForValue().get(key);
        return JsonUtil.parseJson(s, type);
    }
}