package com.rt.platform.infosys.resource.http.exception;

import com.rt.platform.infosys.resource.common.constants.ResourceGlobalConstants;

/**
 * <pre>
 *
 * 【标题】: 参数不合法异常
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017-06-30 16:33
 * </pre>
 */
public class ParameterInValidateException extends ResourceException {

    public ParameterInValidateException(String msg) {
        super(ResourceGlobalConstants.PARAMETER_INVALIDATE_EXCEPTION_CODE, msg);
    }
}
