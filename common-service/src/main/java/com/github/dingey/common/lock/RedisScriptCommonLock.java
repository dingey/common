package com.github.dingey.common.lock;

import com.github.dingey.common.exception.RedisLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

/**
 * 使用lua脚本实现的redis锁
 *
 * @author d
 */
@Component
@SuppressWarnings("unused")
@ConditionalOnClass(StringRedisTemplate.class)
@ConditionalOnProperty(value = "common.lock.enable", havingValue = "true", matchIfMissing = true)
public class RedisScriptCommonLock extends AbstractLuaScript implements CommonLock {
    private final StringRedisTemplate srt;
    private final ThreadLocal<String> lockValue = new ThreadLocal<>();

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    /**
     * 锁的默认超时时间
     */
    @Value("${common.redis.lock.timeout:3000}")
    private long timeout;
    /**
     * redis中锁的过期时间
     */
    @Value("${common.redis.lock.lock-millisecond:30000}")
    private long lockMillisecond;
    /**
     * 获取锁失败时睡眠时间
     */
    @Value("${common.redis.lock.sleep-millisecond:100}")
    private long sleepMillisecond;

    public RedisScriptCommonLock(StringRedisTemplate srt) {
        this.srt = srt;
    }

    private String getLockValue() {
        String s = lockValue.get();
        if (s == null) {
            s = UUID.randomUUID().toString().replaceAll("-", "");
            lockValue.set(s);
        }
        return s;
    }

    @Override
    public void lock(String lockKey) {
        String lockValue = getLockValue();
        boolean acquireLock = tryAcquireLock(lockKey, lockValue, lockMillisecond);
        while (!acquireLock) {
            sleepMoment();
            acquireLock = tryAcquireLock(lockKey, lockValue, lockMillisecond);
        }
    }

    @Override
    public void lock(String lockKey, String lockValue) {
        boolean acquireLock = tryAcquireLock(lockKey, lockValue, lockMillisecond);
        while (!acquireLock) {
            sleepMoment();
            acquireLock = tryAcquireLock(lockKey, lockValue, lockMillisecond);
        }
    }

    private void sleepMoment() {
        try {
            Thread.sleep(sleepMillisecond);
        } catch (InterruptedException e) {
            throw new RedisLockException(e);
        }
    }

    @Override
    public boolean tryLock(String lockKey, long timeout) {
        return tryLock(lockKey, getLockValue(), timeout);
    }

    @Override
    public boolean tryLock(String lockKey, String lockValue, long timeout) {
        boolean acquireLock = tryAcquireLock(lockKey, lockValue, timeout);
        long l1 = System.currentTimeMillis();
        while (!acquireLock) {
            if (System.currentTimeMillis() - l1 > timeout) {
                break;
            }
            sleepMoment();
            acquireLock = tryAcquireLock(lockKey, lockValue, lockMillisecond);
        }
        return acquireLock;
    }

    @Override
    public void unlock(String lockKey) {
        unlock(lockKey, lockValue.get());
    }

    @Override
    public void unlock(String lockKey, String lockValue) {
        boolean releaseLock = tryReleaseLock(lockKey, lockValue);
        if (!releaseLock) {
            if (log.isDebugEnabled()) {
                log.debug("释放锁key:{} 值:{}失败", lockKey, lockValue);
            }
            throw new RedisLockException("释放锁失败");
        }
    }

    private boolean hasLock(String lockKey, String lockValue) {
        return Objects.equals(1L, srt.execute(getHasLockScript(), Collections.singletonList(lockKey), lockValue));
    }

    private boolean tryAcquireLock(String lockKey, String lockValue, long lockTTL) {
        return Objects.equals(1L, srt.execute(getLockScript(), Collections.singletonList(lockKey), lockValue, String.valueOf(lockTTL)));
    }

    private boolean tryReleaseLock(String lockKey, String lockValue) {
        return Objects.equals(1L, srt.execute(getUnlockScript(), Collections.singletonList(lockKey), lockValue));
    }

}
