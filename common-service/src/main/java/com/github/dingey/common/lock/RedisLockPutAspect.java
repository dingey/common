package com.github.dingey.common.lock;

import com.github.dingey.common.annotation.RedisLockPut;
import com.github.dingey.common.util.AspectUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;

/**
 * redis锁
 */
@Aspect
@Component
@ConditionalOnClass(StringRedisTemplate.class)
public class RedisLockPutAspect extends AbstractLockAspect {
    private final Logger log = LoggerFactory.getLogger(RedisLockPutAspect.class);

    @PostConstruct
    public void init() {
        log.info("redis锁初始化完成");
    }

    @Pointcut(value = "@annotation(redisLock)", argNames = "redisLock")
    public void pointcut(RedisLockPut redisLock) {
    }

    @Around(value = "pointcut(redisLock)", argNames = "pjp,redisLock")
    public Object around(ProceedingJoinPoint pjp, RedisLockPut redisLock) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        if (redisLock.condition().isEmpty() || AspectUtil.spel(pjp, redisLock.condition(), boolean.class)) {
            String key = AspectUtil.spel(pjp, redisLock.value(), String.class);
            log.debug("lock method is ( {}.{} ) key is {}.", method.getDeclaringClass().getName(), method.getName(), key);
            if (tryLock(key, redisLock.timelock())) {
                try {
                    return pjp.proceed();
                } finally {
                    if (redisLock.timelock() == 0L) {
                        unLock(key);
                    }
                }
            }
        } else {
            return pjp.proceed();
        }
        return throwException(redisLock.throwable(), redisLock.message(), pjp);
    }
}