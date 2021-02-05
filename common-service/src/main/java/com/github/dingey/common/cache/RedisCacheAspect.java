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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * redis锁
 */
@Aspect
class RedisCacheAspect {
    private final Logger log = LoggerFactory.getLogger(RedisCacheAspect.class);
    private final StringRedisTemplate srt;
    private final static long WAIT_TIMEOUT = 1000L;
    private volatile Map<String, ExpireValue<Object>> cache;
    @Value("${common.local.cache.size:10}")
    private int capacity;

    RedisCacheAspect(StringRedisTemplate srt) {
        this.srt = srt;
    }

    @PostConstruct
    public void init() {
        this.cache = new LinkedHashMap<String, ExpireValue<Object>>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > capacity;
            }
        };
    }

    @Pointcut(value = "@annotation(redisCache)", argNames = "redisCache")
    public void pointcut(RedisCache redisCache) {
    }

    @Around(value = "pointcut(redisCache)", argNames = "pjp,redisCache")
    public Object around(ProceedingJoinPoint pjp, RedisCache redisCache) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(redisCache.cacheName())) {
            sb.append(redisCache.cacheName()).append(":");
        }
        if (StringUtils.hasText(redisCache.key())) {
            sb.append(AspectUtil.spel(pjp, redisCache.key(), String.class));
        } else if (StringUtils.hasText(redisCache.value())) {
            sb.append(AspectUtil.spel(pjp, redisCache.value(), String.class));
        }
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ':') {
            sb.deleteCharAt(sb.length() - 1);
        }
        String key = sb.toString();
        if (StringUtils.isEmpty(key)) {
            key = String.format("%s:%s:%s", method.getDeclaringClass().getName().replace(".", ":"), method.getName(), JsonUtil.toJson(pjp.getArgs()));
        }
        if (hasKey(key)) {
            return getFromCache(key, method.getReturnType(), redisCache);
        } else {
            Object result;
            if (!redisCache.cacheResult()) {
                return pjp.proceed();
            }
            if (redisCache.sync()) {
                boolean b = setLock(key);
                if (b) {
                    if (hasKey(key)) {
                        return getFromCache(key, method.getReturnType(), redisCache);
                    } else {
                        result = pjp.proceed();
                    }
                } else {
                    return tryWaitUntilCached(key, method, redisCache);
                }
            } else {
                result = pjp.proceed();
            }

            if (redisCache.cacheResult()) {
                Map<String, Object> args = Collections.singletonMap("result", result);
                if (StringUtils.isEmpty(redisCache.condition()) || AspectUtil.spel(pjp, redisCache.condition(), boolean.class, args)) {
                    if (StringUtils.isEmpty(redisCache.unless()) || !AspectUtil.spel(pjp, redisCache.unless(), boolean.class, args)) {
                        cacheResult(key, result, redisCache);
                    }
                }
            }
            return result;
        }
    }

    private boolean hasKey(String key) {
        boolean hasKey = hasLocal(key, System.currentTimeMillis());
        if (!hasKey) {
            hasKey = Objects.equals(srt.hasKey(key), true);
        }
        return hasKey;
    }

    private void cacheResult(String key, Object value, RedisCache redisCache) {
        String json = JsonUtil.toJson(value);
        srt.opsForValue().setIfAbsent(key, json, redisCache.expire(), TimeUnit.SECONDS);
        if (log.isDebugEnabled()) {
            log.debug("满足缓存条件，缓存数据,key是{},value:{}", key, json);
        }
        if (redisCache.local() > 0L) {
            log.debug("缓存数据到本地环境,key是{},value:{}", key, json);
            cache.put(key, new ExpireValue<>(1000 * redisCache.local() + System.currentTimeMillis(), value));
        }
    }

    private Object tryWaitUntilCached(String key, Method method, RedisCache redisCache) {
        log.debug("等待其他应用更新缓存，key是{}", key);
        long l = System.currentTimeMillis();
        boolean hasKey = hasKey(key);
        while (!hasKey) {
            try {
                Thread.sleep(10L);
                hasKey = hasKey(key);
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
        return getFromCache(key, method.getReturnType(), redisCache);
    }

    private boolean setLock(String key) {
        return Objects.equals(srt.opsForValue().setIfAbsent("LOCK:" + key, "0", 9000L, TimeUnit.MILLISECONDS), true);
    }

    private Object getFromCache(String key, Class<?> type, RedisCache redisCache) {
        long now = System.currentTimeMillis();
        if (redisCache.local() > 0L && hasLocal(key, now)) {
            return cache.get(key).getData();
        }
        String s = srt.opsForValue().get(key);
        Object o = JsonUtil.parseJson(s, type);
        if (redisCache.local() > 0L) {
            log.debug("缓存数据到本地环境,key是{},value:{}", key, s);
            cache.put(key, new ExpireValue<>(1000 * redisCache.expire() + now, s));
        }
        return o;
    }

    private boolean hasLocal(String key, long expire) {
        ExpireValue<Object> expireValue = cache.get(key);
        if (expireValue != null && expireValue.getExpire() > expire) {
            return true;
        } else if (expireValue != null && expireValue.getExpire() < expire) {
            log.debug("本地缓存已过期,key是{}", key);
            cache.remove(key);
        }
        return false;
    }

    static class ExpireValue<T> {
        private final long expire;
        private final T data;

        public ExpireValue(long expire, T data) {
            this.expire = expire;
            this.data = data;
        }

        public long getExpire() {
            return expire;
        }

        public T getData() {
            return data;
        }
    }

}