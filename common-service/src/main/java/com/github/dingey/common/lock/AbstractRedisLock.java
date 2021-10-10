package com.github.dingey.common.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author d
 */
abstract class AbstractRedisLock extends AbstractLuaScript {
    @Autowired
    StringRedisTemplate srt;
    ThreadLocal<Map<String, LockValue>> holder = new ThreadLocal<>();
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    /**
     * 获取锁失败时睡眠时间
     */
    @Value("${common.redis.lock.sleep-millisecond:100}")
    private long sleepMillisecond;

    private static class LockValue {
        private final String value;
        private int reentrant;

        public LockValue(String value, int reentrant) {
            this.value = value;
            this.reentrant = reentrant;
        }
    }

    /**
     * 尝试获取锁,可重入
     *
     * @param key             锁的key
     * @param lockMillisecond 锁的有效期：毫秒
     * @return 是否获取锁
     */
    public boolean tryLock(String key, long lockMillisecond) {
        LockValue lockValue = getLockValue(key);
        if (lockValue == null) {
            lockValue = new LockValue(UUID.randomUUID().toString().replaceAll("-", ""), 0);
            putLockValue(key, lockValue);
        }
        boolean setIfAbsent = Objects.equals(srt.opsForValue().setIfAbsent(key, lockValue.value, lockMillisecond > 0 ? lockMillisecond : 30000L, TimeUnit.MILLISECONDS), true);
        if (!setIfAbsent) {
            if (lockValue.value.equals(srt.opsForValue().get(key))) {
                srt.expire(key, lockMillisecond, TimeUnit.MILLISECONDS);
                increaseReentrant(lockValue);
                log.debug("尝试获取锁{}成功，当前为重入状态，重入次数加一并延长锁的失效时间", key);
                return true;
            } else {
                log.debug("尝试获取锁{}失败", key);
            }
        } else {
            log.debug("尝试获取锁{}成功，当前为第一次获取锁，重入次数设为一", key);
            increaseReentrant(lockValue);
        }
        return setIfAbsent;
    }

    private LockValue getLockValue(String key) {
        Map<String, LockValue> map = holder.get();
        if (map == null) {
            map = new HashMap<>();
            holder.set(map);
        }
        return map.get(key);
    }

    private void putLockValue(String key, LockValue lockValue) {
        holder.get().put(key, lockValue);
    }

    private void removeLockValue(String key) {
        holder.get().remove(key);
    }

    private void increaseReentrant(LockValue lockValue) {
        lockValue.reentrant++;
    }

    private void decreaseReentrant(LockValue lockValue) {
        lockValue.reentrant--;
    }

    /**
     * 解除锁
     *
     * @param key 锁的key
     */
    public void unlock(String key) {
        LockValue lockValue = getLockValue(key);
        if (lockValue == null) {
            log.debug("当前未持有锁{}，无需释放", key);
            return;
        }
        if (lockValue.reentrant > 1) {
            log.debug("当前持有锁{}重入次数{}大于1，递减重入次数", key, lockValue.reentrant);
            decreaseReentrant(lockValue);
            return;
        }
        srt.execute(getUnlockScript(), Collections.singletonList(key), lockValue.value);
        removeLockValue(key);
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
            long remainder = System.currentTimeMillis() - start;
            if (remainder > timeout) {
                log.debug("自旋获取锁 {} 失败，超时 {} 毫秒", key, remainder);
                return false;
            }
            try {
                Thread.sleep(sleepMillisecond);
            } catch (InterruptedException ignore) {
            }
        }
        log.debug("自旋获取锁 {} 成功，耗时 {} 毫秒", key, System.currentTimeMillis() - start);
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
