package com.github.dingey.common.zipkin;

import brave.Tracer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "spring.sleuth.enabled")
@ConditionalOnClass(Tracer.class)
public class ZipkinTraceConfiguration {
    @Bean
    public ZipkinTracerAspect zipkinTracerAspect(ObjectMapper objectMapper, Tracer tracer) {
        return new ZipkinTracerAspect(objectMapper, tracer);
    }
}
