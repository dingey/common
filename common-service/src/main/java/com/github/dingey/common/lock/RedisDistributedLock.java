package com.github.dingey.common.lock;

import com.github.dingey.common.exception.CommonLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("unused")
public class RedisDistributedLock implements DistributedLock {
    private StringRedisTemplate srt;
    private ThreadLocal<String> lockValue = new ThreadLocal<>();
    private DefaultRedisScript<Long> lockScript;
    private DefaultRedisScript<Long> unlockScript;
    private DefaultRedisScript<Long> hasLockScript;
    private static final Logger log = LoggerFactory.getLogger(RedisDistributedLock.class);

    public RedisDistributedLock(StringRedisTemplate srt) {
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
        boolean acquireLock = tryAcquireLock(lockKey, lockValue, 30000L);
        while (!acquireLock) {
            acquireLock = tryAcquireLock(lockKey, lockValue, 30000L);
        }
    }

    @Override
    public void lock(String lockKey, String lockValue) {
        boolean acquireLock = tryAcquireLock(lockKey, lockValue, 30000L);
        while (!acquireLock) {
            acquireLock = tryAcquireLock(lockKey, lockValue, 30000L);
        }
    }

    @Override
    public boolean tryLock(String lockKey, long time) {
        return tryLock(lockKey, getLockValue(), time);
    }

    @Override
    public boolean tryLock(String lockKey, String lockValue, long time) {
        boolean acquireLock = tryAcquireLock(lockKey, lockValue, time);
        long l1 = System.currentTimeMillis();
        while (!acquireLock) {
            if (System.currentTimeMillis() - l1 > time) {
                break;
            }
            acquireLock = tryAcquireLock(lockKey, lockValue, time);
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
            throw new CommonLockException("释放锁失败");
        }
    }

    private boolean hasLock(String lockKey, String lockValue) {
        return Objects.equals(1L, srt.execute(getHasLockScript(), Collections.singletonList(lockKey), lockValue));
    }

    private boolean tryAcquireLock(String lockKey, String lockValue, long time) {
        return Objects.equals(1L, srt.execute(getLockScript(), Collections.singletonList(lockKey), Arrays.asList(lockValue, time)));
    }

    private boolean tryReleaseLock(String lockKey, String lockValue) {
        return Objects.equals(1L, srt.execute(getUnlockScript(), Collections.singletonList(lockKey), lockValue));
    }

    private DefaultRedisScript<Long> getHasLockScript() {
        if (hasLockScript == null) {
            synchronized (this) {
                if (hasLockScript == null) {
                    hasLockScript = new DefaultRedisScript<>(
                            "local key     = KEYS[1]" +
                                    "local content = ARGV[1]" +
                                    "local value = redis.call('get', key)" +
                                    "if value == content then" +
                                    "  return 1" +
                                    "else" +
                                    "    return 0" +
                                    "end", Long.class);
                }
            }
        }
        return hasLockScript;
    }

    private DefaultRedisScript<Long> getLockScript() {
        if (lockScript == null) {
            synchronized (this) {
                if (lockScript == null) {
                    lockScript = new DefaultRedisScript<>(
                            //--  如果获取锁成功，则返回 1
                            "local key     = KEYS[1]" +
                                    "local content = ARGV[1]" +
                                    "local ttl     = tonumber(ARGV[2])" +
                                    "local lockSet = redis.call('setnx', key, content)" +
                                    "if lockSet == 1 then" +
                                    "  redis.call('PEXPIRE', key, ttl)" +
                                    "else" +
                                    // "  -- 如果value相同，则认为是同一个线程的请求，则认为重入锁" +
                                    "  local value = redis.call('get', key)" +
                                    "  if(value == content) then" +
                                    "    lockSet = 1;" +
                                    "    redis.call('PEXPIRE', key, ttl)" +
                                    "  end" +
                                    "end" +
                                    "return lockSet", Long.class);
                }
            }
        }
        return lockScript;
    }

    private DefaultRedisScript<Long> getUnlockScript() {
        if (unlockScript == null) {
            synchronized (this) {
                if (unlockScript == null) {
                    unlockScript = new DefaultRedisScript<>(
                            //--  如果获取锁成功，则返回 1
                            "local key     = KEYS[1]" +
                                    "local content = ARGV[1]" +
                                    "local value = redis.call('get', key)" +
                                    "if value == content then" +
                                    "  return redis.call('del', key)" +
                                    "else" +
                                    "    return 0" +
                                    "end", Long.class);
                }
            }
        }
        return unlockScript;
    }

}
