package com.github.dingey.common.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnClass(WebMvcConfigurer.class)
public class GlobalContextWebMvcConfiguration implements WebMvcConfigurer {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (log.isInfoEnabled()) {
            log.info("Initializing GlobalContext Webmvc Filter");
        }
        registry.addInterceptor(new GlobalContextInterceptor());
    }
}
