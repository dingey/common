package com.github.dingey.common.lock;

import com.github.dingey.common.exception.RedisLockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 可重入redis锁
 *
 * @author d
 * @since 0.0.3
 */
@Component
@SuppressWarnings("unused")
@ConditionalOnClass(StringRedisTemplate.class)
@ConditionalOnProperty(value = "common.redis.lock.enable", havingValue = "true", matchIfMissing = true)
public class SimpleRedisLock extends AbstractRedisLock {
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

    public void lock(String key) throws RedisLockException {
        boolean lock = trySpinLock(key, lockMillisecond, timeout);
        if (!lock) {
            throw new RedisLockException("服务器繁忙，请稍后再试L。");
        }
    }

    public boolean tryLock(String key) {
        return super.tryLock(key, lockMillisecond);
    }
}
