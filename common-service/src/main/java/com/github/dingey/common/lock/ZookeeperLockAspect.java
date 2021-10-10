package com.github.dingey.common.lock;

import com.github.dingey.common.annotation.ZookeeperLock;
import com.github.dingey.common.exception.RedisLockException;
import com.github.dingey.common.util.AspectUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;


/**
 * @author d
 * @since 0.0.3
 */
@Aspect
@Order(2)
@Component
@ConditionalOnBean(CuratorFramework.class)
@ConditionalOnClass({CuratorFramework.class, InterProcessLock.class, ProceedingJoinPoint.class})
@ConditionalOnProperty(value = "common.lock.zk.enable", havingValue = "true", matchIfMissing = true)
class ZookeeperLockAspect {
    private final CuratorFramework curatorFramework;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public ZookeeperLockAspect(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    @PostConstruct
    public void init() {
        log.info("初始化zookeeper锁");
    }

    @Pointcut(value = "@annotation(zookeeperLock)", argNames = "zookeeperLock")
    public void pointcut(ZookeeperLock zookeeperLock) {
    }

    @Around(value = "pointcut(zookeeperLock)", argNames = "pjp,zookeeperLock")
    public Object around(ProceedingJoinPoint pjp, ZookeeperLock zookeeperLock) throws Throwable {
        if (zookeeperLock.condition().isEmpty() || AspectUtil.spel(pjp, zookeeperLock.condition(), boolean.class)) {
            Method method = ((MethodSignature) pjp.getSignature()).getMethod();
            String key = zookeeperLock.value().isEmpty() ? (method.getDeclaringClass().getName() + "." + method.getName()) : AspectUtil.spel(pjp, zookeeperLock.value(), String.class);

            if (log.isDebugEnabled()) {
                log.debug("zookeeper锁{}，满足加锁条件", key);
            }
            InterProcessLock lock = new InterProcessMutex(curatorFramework, key);
            try {
                if (zookeeperLock.timeout() > 0L) {
                    if (lock.acquire(zookeeperLock.timeout(), TimeUnit.MILLISECONDS)) {
                        if (log.isDebugEnabled()) {
                            log.debug("zookeeper锁{}，加锁成功{}", key, zookeeperLock.timeout());
                        }
                        return pjp.proceed();
                    }
                } else {
                    lock.acquire();
                    if (log.isDebugEnabled()) {
                        log.debug("zookeeper锁{}，加锁成功{}", key, zookeeperLock.timeout());
                    }
                    return pjp.proceed();
                }
            } finally {
                if (lock.isAcquiredInThisProcess()) {
                    lock.release();
                    if (log.isDebugEnabled()) {
                        log.debug("zookeeper锁{}，解锁成功", key);
                    }
                }
            }
        } else {
            return pjp.proceed();
        }
        if (zookeeperLock.throwable()) {
            if (zookeeperLock.message().isEmpty()) {
                throw new RedisLockException("服务器繁忙，请稍后再试L。");
            } else {
                throw new RedisLockException(zookeeperLock.message().contains("#") ? AspectUtil.spel(pjp, zookeeperLock.message(), String.class) : zookeeperLock.message());
            }
        } else {
            return null;
        }
    }
}
