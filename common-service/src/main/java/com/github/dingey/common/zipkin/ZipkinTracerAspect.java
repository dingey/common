package com.github.dingey.common.zipkin;

import brave.Span;
import brave.Tracer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;

@Aspect
class ZipkinTracerAspect {
    @Value("{common.zipkin.tag.param:true}")
    private boolean tagParam;
    @Value("{common.zipkin.tag.result:true}")
    private boolean tagResult;
    @Value("{common.zipkin.tag.error:true}")
    private boolean tagError;
    private final ObjectMapper objectMapper;
    private final Tracer tracer;

    ZipkinTracerAspect(ObjectMapper objectMapper, Tracer tracer) {
        this.objectMapper = objectMapper;
        this.tracer = tracer;
    }

    @Around("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Span span = tracer.currentSpan();
        tagParam(span, pjp.getArgs());
        Object result = null;
        try {
            result = pjp.proceed();
        } catch (Throwable t) {
            tagError(span, t);
            throw t;
        } finally {
            if (tagResult) {
                span.tag("result", toJson(result));
            }
        }
        return result;
    }

    private void tagParam(Span span, Object args) {
        if (tagParam) {
            span.tag("param", toJson(args));
        }
    }

    private void tagError(Span span, Throwable t) {
        if (tagError) {
            span.tag("error-msg", t.getMessage());
            span.tag("stack-trace[0]", toJson(t.getStackTrace()[0]));
        }
    }

    private String toJson(Object v) {
        try {
            return objectMapper.writeValueAsString(v);
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }
}
