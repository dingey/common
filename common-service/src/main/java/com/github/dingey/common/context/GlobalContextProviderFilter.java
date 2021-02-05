package com.github.dingey.common.context;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author d
 */
@Activate(group = Constants.PROVIDER)
public class GlobalContextProviderFilter implements Filter {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String json = RpcContext.getContext().getAttachment(GlobalContext.HEADER_NAME);
        GlobalContext.setByJsonString(json);

        if (log.isDebugEnabled()) {
            log.debug("提供者传递全局上下文参数，值为：{}", json);
        }
        return invoker.invoke(invocation);
    }
}
