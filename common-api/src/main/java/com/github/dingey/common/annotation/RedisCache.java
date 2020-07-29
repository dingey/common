package com.github.dingey.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisCache {
    /**
     * 缓存key前缀
     */
    String value() default "";

    /**
     * 缓存key前缀
     */
    String cacheName() default "";

    /**
     * 缓存key
     */
    String key() default "";

    /**
     * 用于制作方法的Spring表达式语言（SpEL）表达式
     * 有条件地缓存。
     * <p>默认为{@code  }，表示方法结果始终被缓存。
     */
    String condition() default "";

    /**
     * Spring表达式语言（SpEL）表达式用于否决方法缓存。
     * <p>与{@link #condition}不同，此表达式在方法之后求值
     * 已被调用，因此可以引用{@code result}。
     */
    String unless() default "";

    /**
     * 是否异步
     * 如果有多个线程，则同步底层方法的调用
     */
    boolean sync() default false;

    /**
     * 过期时间,单位 秒, 默认600秒
     */
    long expire() default 600;

    /**
     * 如果缓存不存在，是否缓存执行结果
     */
    boolean cacheResult() default true;

    /**
     * 是否支持本地缓存
     */
    boolean local() default false;
}