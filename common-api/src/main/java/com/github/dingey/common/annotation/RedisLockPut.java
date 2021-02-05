package com.github.dingey.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * redis锁,支持SPEL表达式
 * <p>
 * 用法：<pre>
 *    当 id = 1
 * 1 {@code @RedisLock("'test'+#id")}  限制key为test1的同一刻只有一个线程执行
 * 2 {@code @RedisLock(value="'test'+#id",timeout=500L)}  如果锁过期时间小于500毫秒则睡眠过期时间再尝试获取锁
 * 3 {@code @RedisLock(value="'test'+#id",timeout=500L,timelock=6000L)}  锁住6秒,如果锁过期时间小于500毫秒则睡眠过期时间再尝试获取锁
 * 4 {@code @RedisLock(value="'test'+#id",condition="#id>5")} 只有id大于5才会锁住方法
 *   {@code public List list(int id){}}
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisLockPut {

    /**
     * 锁住时长(毫秒)默认0,即方法执行完成即解锁，大于0则不解锁
     */
    long timelock() default 0L;

    /**
     * 锁的value值，支持spel表达式
     */
    String value();

    /* 锁的value值，支持spel表达式 */
    //String key() default "";

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