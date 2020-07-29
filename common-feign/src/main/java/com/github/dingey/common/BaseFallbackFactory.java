package com.github.dingey.common;

public class BaseFallbackFactory extends BaseFallback {
    @Override
    public void error(Throwable t, Object... args) {
        super.error(t, args);
    }

    @Override
    public StackTraceElement getTrace() {
        return super.getTrace(5);
    }

    public StackTraceElement getErrorTrace() {
        return super.getTrace(5);
    }

    public Object empty() {
        return super.empty();
    }
}
