package com.github.dingey.common.context;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * @author d
 */
@Activate(group = Constants.CONSUMER)
public class GlobalContextConsumerFilter implements Filter {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        String json = GlobalContext.toJsonString();
        RpcContext.getContext().setAttachments(Collections.singletonMap(GlobalContext.HEADER_NAME, json));

        if (log.isDebugEnabled()) {
            log.debug("消费者传递全局上下文参数，值为：{}", json);
        }
        return invoker.invoke(invocation);
    }
}
