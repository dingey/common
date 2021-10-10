package com.github.dingey.common.async;

import com.github.dingey.common.context.GlobalContext;
import org.springframework.core.task.TaskDecorator;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;

public class GlobalContextCopyingDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> contextMap = GlobalContext.getContextMap();
        return () -> {
            try {
                GlobalContext.setContextMap(contextMap);
                runnable.run();
            } finally {
                GlobalContext.clear();
                RequestContextHolder.resetRequestAttributes();
            }
        };
    }
}
