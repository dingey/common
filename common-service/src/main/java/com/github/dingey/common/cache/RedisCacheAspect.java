package com.github.dingey.common.cache;

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
import org.springframework.data.redis.core.StringRedisTemplate;
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
class RedisCacheAspect {
    private final Logger log = LoggerFactory.getLogger(RedisCacheAspect.class);
    private final StringRedisTemplate srt;
    private final static long WAIT_TIMEOUT = 1000L;
    private final LocalCache<String, Object> cache;

    RedisCacheAspect(StringRedisTemplate srt, LocalCache<String, Object> localCache) {
        this.srt = srt;
        this.cache = localCache;
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
        if (hasKey(key, redisCache.local())) {
            return getFromCache(key, method.getReturnType(), redisCache.local());
        } else {
            Object result;
            if (!redisCache.cacheResult()) {
                return pjp.proceed();
            }
            if (redisCache.sync()) {
                boolean b = setLock(key);
                if (b) {
                    if (hasKey(key, redisCache.local())) {
                        return getFromCache(key, method.getReturnType(), false);
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
                    cacheResult(key, s, redisCache.expire(), redisCache.local());
                    log.debug("满足缓存条件，缓存数据,key是{},value:{}", key, s);
                }
            }
            return result;
        }
    }

    private boolean hasKey(String key, boolean caffeine) {
        if (!caffeine) {
            return Objects.equals(srt.hasKey(key), true);
        }
        boolean hasKey = cache.hasKey(key);
        if (!hasKey) {
            hasKey = Objects.equals(srt.hasKey(key), true);
        }
        return hasKey;
    }

    private void cacheResult(String key, String value, long expire, boolean localCache) {
        srt.opsForValue().setIfAbsent(key, value, expire, TimeUnit.SECONDS);
        if (localCache) {
            cache.set(key, value, expire);
        }
    }

    private Object tryWaitUntilCacheed(String key, Method method) {
        log.debug("等待其他应用更新缓存，key是{}", key);
        long l = System.currentTimeMillis();
        boolean hasKey = hasKey(key, false);
        while (!hasKey) {
            try {
                Thread.sleep(10L);
                hasKey = hasKey(key, false);
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
        return getFromCache(key, method.getReturnType(), false);
    }

    private boolean setLock(String key) {
        return Objects.equals(srt.opsForValue().setIfAbsent("LOCK:" + key, "0", 9000L, TimeUnit.MILLISECONDS), true);
    }

    private Object getFromCache(String key, Class<?> type, boolean localCache) {
        String s = null;
        if (localCache && cache != null) {
            s = (String) cache.get(key);
        }
        if (StringUtils.isEmpty(s)) {
            s = srt.opsForValue().get(key);
            if (cache != null) {
                cache.set(key,s);
            }
        }
        return JsonUtil.parseJson(s, type);
    }
}