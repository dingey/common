package com.github.dingey.common.lock;

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
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;

@Aspect
@Component
@ConditionalOnClass(StringRedisTemplate.class)
@Order(1)
class RedisLockAspect extends AbstractLockAspect {
    private final Logger log = LoggerFactory.getLogger(RedisLockAspect.class);


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

}
