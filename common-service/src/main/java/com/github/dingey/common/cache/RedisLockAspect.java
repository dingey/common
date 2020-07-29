package com.github.dingey.common.cache;

import com.github.dingey.common.annotation.RedisLock;
import com.github.dingey.common.exception.RedisLockException;
import com.github.dingey.common.util.AspectUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Aspect
@Order(1)
class RedisLockAspect {
    private final Logger log = LoggerFactory.getLogger(RedisLockAspect.class);
    private final StringRedisTemplate srt;
    /**
     * 释放锁lua脚本
     */
    private static final String RELEASE_LOCK_LUA_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    private ThreadLocal<String> lockValue = new ThreadLocal<>();

    @PostConstruct
    public void init() {
        log.debug("redis锁初始化完成");
    }

    RedisLockAspect(StringRedisTemplate srt) {
        this.srt = srt;
    }

    @Pointcut(value = "@annotation(redisLock)", argNames = "redisLock")
    public void pointcut(RedisLock redisLock) {
    }

    @Around(value = "pointcut(redisLock)", argNames = "pjp,redisLock")
    public Object around(ProceedingJoinPoint pjp, RedisLock redisLock) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        if (redisLock.condition().isEmpty() || AspectUtil.spel(pjp, redisLock.condition(), boolean.class)) {
            String key = redisLock.value().isEmpty() ? (method.getDeclaringClass().getName() + "." + method.getName()) : AspectUtil.spel(pjp, redisLock.value(), String.class);
            log.debug("lock method is ( {}.{} ) key is {}.", method.getDeclaringClass().getName(), method.getName(), key);
            if (tryLock(key, redisLock.timelock())) {
                try {
                    return pjp.proceed();
                } finally {
                    if (redisLock.timelock() == 0L) {
                        unLock(key);
                    }
                }
            } else if (redisLock.timeout() > 0) {
                if (redisLock.spinLock() && trySpinLock(key, redisLock.timelock(), redisLock.timeout())) {
                    try {
                        return pjp.proceed();
                    } finally {
                        if (redisLock.timelock() == 0L) {
                            unLock(key);
                        }
                    }
                } else if (!redisLock.spinLock() && trySleepRetryLock(key, redisLock.timelock(), redisLock.timeout())) {
                    try {
                        return pjp.proceed();
                    } finally {
                        if (redisLock.timelock() == 0L) {
                            unLock(key);
                        }
                    }
                }
            }
        } else {
            return pjp.proceed();
        }
        if (redisLock.throwable()) {
            if (redisLock.message().isEmpty()) {
                throw new RedisLockException("服务器繁忙，请稍后再试L。");
            } else {
                throw new RedisLockException(redisLock.message().contains("#") ? AspectUtil.spel(pjp, redisLock.message(), String.class) : redisLock.message());
            }
        } else {
            return null;
        }
    }

    /**
     * 尝试获取锁,可重入
     *
     * @param key             锁的key
     * @param lockMillisecond 锁的有效期：毫秒
     * @return 是否获取锁
     */
    private boolean tryLock(String key, long lockMillisecond) {
        String value = lockValue.get();
        if (StringUtils.isEmpty(lockValue)) {
            value = UUID.randomUUID().toString().replaceAll("-", "");
            lockValue.set(value);
        }
        boolean setIfAbsent = Objects.equals(srt.opsForValue().setIfAbsent(key, value, lockMillisecond, TimeUnit.MILLISECONDS), true);
        if (!setIfAbsent) {
            if (value.equals(srt.opsForValue().get(key))) {
                srt.expire(key, lockMillisecond, TimeUnit.MILLISECONDS);
                return true;
            }
        }
        return setIfAbsent;
    }

    /**
     * 解除锁
     *
     * @param key 锁的key
     */
    private void unLock(String key) {
        if (StringUtils.isEmpty(lockValue.get())) {
            return;
        }
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(RELEASE_LOCK_LUA_SCRIPT, Long.class);
        srt.execute(redisScript, Collections.singletonList(key), lockValue.get());
    }

    /**
     * 自旋获取锁
     *
     * @param key             锁的key
     * @param lockMillisecond 锁的有效期：毫秒
     * @param timeout         超时时间：毫秒
     * @return 是否获取锁
     */
    private boolean trySpinLock(String key, long lockMillisecond, long timeout) {
        long start = System.currentTimeMillis();
        while (!tryLock(key, lockMillisecond)) {
            if ((System.currentTimeMillis() - start) > timeout) {
                return false;
            }
            try {
                Thread.sleep(10L);
            } catch (InterruptedException ignore) {
            }
        }
        return true;
    }

    /**
     * 如果锁失效时间在超时时间内，睡眠当前线程再尝试获取锁
     *
     * @param key             锁的key
     * @param lockMillisecond 锁的有效期：毫秒
     * @param timeout         超时时间：毫秒
     * @return 是否获取锁
     */
    private boolean trySleepRetryLock(String key, long lockMillisecond, long timeout) {
        boolean tryLock = tryLock(key, lockMillisecond);
        if (!tryLock) {
            //剩余生存时间
            Long ttl = srt.getExpire(key);
            if (ttl == null || ttl <= 0L) {
                tryLock = tryLock(key, lockMillisecond);
            } else if (ttl <= timeout) {
                try {
                    Thread.sleep(ttl);
                    tryLock = tryLock(key, lockMillisecond);
                } catch (InterruptedException e) {
                    return false;
                }
            }
        }
        return tryLock;
    }
}
