package com.github.dingey.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisEvict {
    /**
     * 缓存key：SpEL表达式或字符常量
     */
    String[] value() default {};

    /**
     * 执行条件：SpEL表达式
     */
    String condition() default "";

    /**
     * 方法执行前清除缓存
     */
    boolean beforeInvocation() default false;
    /**
     * 是否支持本地缓存
     */
    boolean local() default false;
}
