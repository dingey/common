package com.github.dingey.common.lock;

import org.springframework.data.redis.core.script.DefaultRedisScript;

class AbstractLuaScript {
    private DefaultRedisScript<Long> lockScript;
    private DefaultRedisScript<Long> unlockScript;
    private DefaultRedisScript<Long> hasLockScript;

    DefaultRedisScript<Long> getHasLockScript() {
        if (hasLockScript == null) {
            synchronized (this) {
                if (hasLockScript == null) {//--  如果获取锁成功，则返回 1
                    hasLockScript = new DefaultRedisScript<>(HAS_LOCK_LUA, Long.class);
                }
            }
        }
        return hasLockScript;
    }

    DefaultRedisScript<Long> getLockScript() {
        if (lockScript == null) {
            synchronized (this) {
                if (lockScript == null) {  //--  如果获取锁成功，则返回 1
                    lockScript = new DefaultRedisScript<>(LOCK_LUA, Long.class);
                }
            }
        }
        return lockScript;
    }

    DefaultRedisScript<Long> getUnlockScript() {
        if (unlockScript == null) {
            synchronized (this) {
                if (unlockScript == null) {//--  如果获取锁成功，则返回 1
                    unlockScript = new DefaultRedisScript<>(UNLOCK_LUA, Long.class);
                }
            }
        }
        return unlockScript;
    }

    /**
     * 持有锁lua脚本
     */
    static final String HAS_LOCK_LUA = "local key     = KEYS[1]\n" +
            "local content = ARGV[1]\n" +
            "local value = redis.call('get', key)\n" +
            "if value == content then\n" +
            "  return 1\n" +
            "else\n" +
            "    return 0\n" +
            "end\n";
    /**
     * 锁lua脚本
     */
    static final String LOCK_LUA = "local key     = KEYS[1]\n" +
            "local content = ARGV[1]\n" +
            "local ttl     = tonumber(ARGV[2])\n" +
            "local lockSet = redis.call('setnx', key, content)\n" +
            "if lockSet == 1 then\n" +
            "  redis.call('PEXPIRE', key, ttl)\n" +
            "else\n" +
            // "  -- 如果value相同，则认为是同一个线程的请求，则认为重入锁" +
            "  local value = redis.call('get', key)\n" +
            "  if(value == content) then\n" +
            "    lockSet = 1;\n" +
            "    redis.call('PEXPIRE', key, ttl)\n" +
            "  end\n" +
            "end\n" +
            "return lockSet\n";
    /**
     * 释放锁lua脚本
     */
    static final String UNLOCK_LUA = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

}
