package com.github.dingey.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@ConditionalOnClass(ObjectMapper.class)
@AutoConfigureAfter(ObjectMapper.class)
public class CommonUtilAutoConfiguration {
    @Resource
    private ObjectMapper objectMapper;

    @Bean
    public JsonUtil jsonUtil() {
        return new JsonUtil(objectMapper);
    }
}
