package com.github.dingey.common.lock;

import com.github.dingey.common.exception.RedisLockException;
import com.github.dingey.common.util.AspectUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

abstract class AbstractLockAspect {
    @Autowired
    StringRedisTemplate srt;
    ThreadLocal<String> lockValue = new ThreadLocal<>();
    /**
     * 释放锁lua脚本
     */
    private static final String RELEASE_LOCK_LUA_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    Object throwException(boolean throwable, String message, ProceedingJoinPoint pjp) {
        if (throwable) {
            if (message.isEmpty()) {
                throw new RedisLockException("服务器繁忙，请稍后再试L。");
            } else {
                throw new RedisLockException(message.contains("#") ? AspectUtil.spel(pjp, message, String.class) : message);
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
    boolean tryLock(String key, long lockMillisecond) {
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
    void unLock(String key) {
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
    boolean trySpinLock(String key, long lockMillisecond, long timeout) {
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
    boolean trySleepRetryLock(String key, long lockMillisecond, long timeout) {
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
