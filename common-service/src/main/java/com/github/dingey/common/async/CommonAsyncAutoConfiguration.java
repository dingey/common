package com.github.dingey.common.async;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@RefreshScope
@Configuration
@ConditionalOnProperty(value = "common.async.enable", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(ThreadPoolTaskExecutor.class)
public class CommonAsyncAutoConfiguration {
    @Value("${common.async.core-pool-size:1}")
    private int corePoolSize;
    @Value("${common.async.max-pool-size:3}")
    private int maxPoolSize;
    @Value("${common.async.queue-capacity:100}")
    private int queueCapacity;
    @Value("${common.async.thread-name-prefix:AsyncThread-}")
    private String threadNamePrefix;

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setTaskDecorator(new GlobalContextCopyingDecorator());
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }
}
