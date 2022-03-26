package com.github.dingey.common.exception;

import com.github.dingey.common.ResultCode;

public class BizException extends CommonException {
    public BizException(ResultCode resultCode) {
        super(resultCode);
    }
}
