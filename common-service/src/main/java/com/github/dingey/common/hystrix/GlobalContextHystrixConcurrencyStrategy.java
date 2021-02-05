package com.github.dingey.common.hystrix;

import com.github.dingey.common.context.GlobalContext;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.Callable;

@Configuration
@ConditionalOnClass(HystrixConcurrencyStrategy.class)
public class GlobalContextHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {
    private final Logger log = LoggerFactory.getLogger(GlobalContextHystrixConcurrencyStrategy.class);

    private HystrixConcurrencyStrategy delegate;

    public GlobalContextHystrixConcurrencyStrategy() {
        try {
            this.delegate = HystrixPlugins.getInstance().getConcurrencyStrategy();
            if (this.delegate instanceof GlobalContextHystrixConcurrencyStrategy) {
                return;
            }
            HystrixCommandExecutionHook commandExecutionHook =
                    HystrixPlugins.getInstance().getCommandExecutionHook();
            HystrixEventNotifier eventNotifier = HystrixPlugins.getInstance().getEventNotifier();
            HystrixMetricsPublisher metricsPublisher = HystrixPlugins.getInstance().getMetricsPublisher();
            HystrixPropertiesStrategy propertiesStrategy =
                    HystrixPlugins.getInstance().getPropertiesStrategy();
            this.logCurrentStateOfHystrixPlugins(eventNotifier, metricsPublisher, propertiesStrategy);
            HystrixPlugins.reset();
            HystrixPlugins.getInstance().registerConcurrencyStrategy(this);
            HystrixPlugins.getInstance().registerCommandExecutionHook(commandExecutionHook);
            HystrixPlugins.getInstance().registerEventNotifier(eventNotifier);
            HystrixPlugins.getInstance().registerMetricsPublisher(metricsPublisher);
            HystrixPlugins.getInstance().registerPropertiesStrategy(propertiesStrategy);
            if (log.isInfoEnabled()) {
                log.info("Initializing GlobalContext Feign Hystrix Strategy support");
            }
        } catch (Exception e) {
            log.error("Failed to register GlobalContext Hystrix Strategy support", e);
        }
    }

    @Override
    public <T> Callable<T> wrapCallable(Callable<T> callable) {
        Map<String, String> contextMap = GlobalContext.getContextMap();
        return new WrappedCallable<>(callable, contextMap);
    }

    static class WrappedCallable<T> implements Callable<T> {
        private final Callable<T> target;
        private final Map<String, String> contextMap;

        public WrappedCallable(Callable<T> target, Map<String, String> contextMap) {
            this.target = target;
            this.contextMap = contextMap;
        }

        @Override
        public T call() throws Exception {
            try {
                GlobalContext.setContextMap(contextMap);
                return target.call();
            } finally {
                GlobalContext.clear();
            }
        }
    }

    private void logCurrentStateOfHystrixPlugins(HystrixEventNotifier eventNotifier,
                                                 HystrixMetricsPublisher metricsPublisher, HystrixPropertiesStrategy propertiesStrategy) {
        if (log.isDebugEnabled()) {
            log.debug("Current Hystrix plugins configuration is [" + "concurrencyStrategy ["
                    + this.delegate + "]," + "eventNotifier [" + eventNotifier + "]," + "metricPublisher ["
                    + metricsPublisher + "]," + "propertiesStrategy [" + propertiesStrategy + "]," + "]");
            log.debug("Registering GlobalContext Hystrix Concurrency Strategy.");
        }
    }
}
