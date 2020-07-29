package com.github.dingey.common;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({CommonRegistrar.class})
public @interface EnableCommon {
}
