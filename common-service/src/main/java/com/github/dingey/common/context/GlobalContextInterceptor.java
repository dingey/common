package com.github.dingey.common.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GlobalContextInterceptor implements HandlerInterceptor {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String string = request.getHeader(GlobalContext.HEADER_NAME);
        if (StringUtils.hasText(string)) {
            log.debug("拦截到全局上下文标识{}，值为:{}", GlobalContext.HEADER_NAME, string);
            GlobalContext.setByJsonString(string);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (StringUtils.hasText(request.getHeader(GlobalContext.HEADER_NAME))) {
            log.debug("请求结束，清除全局上下文标识");
            GlobalContext.clear();
        }
    }
}
