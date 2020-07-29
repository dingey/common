package com.github.dingey.common.zipkin;

import brave.Span;
import brave.Tracer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
class ZipkinTracerAspect {

    private final ObjectMapper objectMapper;
    private final Tracer tracer;

    ZipkinTracerAspect(ObjectMapper objectMapper, Tracer tracer) {
        this.objectMapper = objectMapper;
        this.tracer = tracer;
    }

    @Around("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Span span = tracer.currentSpan();
        span.tag("param", objectMapper.writeValueAsString(pjp.getArgs()));
        Object result = null;
        try {
            result = pjp.proceed();
        } finally {
            span.tag("result", objectMapper.writeValueAsString(result));
        }
        return result;
    }
}
