package com.github.dingey.common.lock;

import com.github.dingey.common.exception.RedisLockException;
import com.github.dingey.common.util.AspectUtil;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author d
 */
abstract class AbstractLockAspect extends AbstractRedisLock {

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
}
