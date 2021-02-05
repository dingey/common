package com.github.dingey.common.context;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnClass(RequestInterceptor.class)
@Configuration
public class GlobalContextFeignConfiguration {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Bean
    public EasyTccFeignRequestInterceptor easyTccFeignRequestInterceptor() {
        if (log.isInfoEnabled()) {
            log.info("Initializing GlobalContext Feign client support");
        }
        return new EasyTccFeignRequestInterceptor();
    }

    static class EasyTccFeignRequestInterceptor implements RequestInterceptor {
        protected transient Log logger = LogFactory.getLog(this.getClass());

        @Override
        public void apply(RequestTemplate requestTemplate) {
            logger.debug("判断是否传递全局上下文信息");
            if (GlobalContext.hasContext()) {
                logger.debug("判断是否传递全局上下文信息" + GlobalContext.getContextMap());
                requestTemplate.header(GlobalContext.HEADER_NAME, GlobalContext.toJsonString());
            }
        }
    }
}
