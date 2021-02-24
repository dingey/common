package com.github.dingey.common.annotation;

import java.lang.annotation.*;

/**
 * @author d
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ZookeeperLock {
    /**
     * 锁的value值，支持spel表达式
     */
    String value() default "";

    /**
     * 超时(毫秒)，等待多久，默认10秒，为0会一直等待
     */
    long timeout() default 10000L;

    /**
     * 锁的满足条件，支持spel表达式
     */
    String condition() default "";

    /**
     * 提示内容，支持spel表达式
     */
    String message() default "";

    /**
     * 是否以异常的形式抛出
     */
    boolean throwable() default true;
}
