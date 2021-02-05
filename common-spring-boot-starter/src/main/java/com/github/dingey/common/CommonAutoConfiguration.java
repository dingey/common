package com.github.dingey.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@EnableCommon
@Configuration
public class CommonAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(CommonAutoConfiguration.class);

    @PostConstruct
    public void init() {
        if (log.isInfoEnabled()) {
            log.debug("Initializing Common Service");
        }
    }
}
