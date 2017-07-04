package com.rt.platform.infosys.resource.common.exception;

import com.rt.platform.infosys.base.common.enums.ExceptionSysMarkEnum;
import com.rt.platform.infosys.base.common.exception.BusinessBaseException;
import com.rt.platform.infosys.resource.common.constants.ResourceGlobalConstants;
import com.rt.platform.infosys.resource.common.enums.ResourceExceptionCodeEnum;

/**
 * <pre>
 *  
 *
 * 【标题】: 资源中心业务异常
 * 【描述】: 
 * 【版权】: 润投科技
 * 【作者】: 唐宋  
 * 【时间】: 2017年6月21日 上午10:04:13
 * </pre>
 */
public class ResourceBusinessException extends BusinessBaseException {
    private static final long serialVersionUID = 1L;

    public ResourceBusinessException(String code) {
        super(ExceptionSysMarkEnum.RESOURCE_MARK, ResourceGlobalConstants.APPID, code,
                ResourceExceptionCodeEnum.getMsg(code));
    }

    public ResourceBusinessException(String code, Throwable cause) {
        super(ExceptionSysMarkEnum.RESOURCE_MARK, ResourceGlobalConstants.APPID, code,
                ResourceExceptionCodeEnum.getMsg(code), cause);
    }

    public ResourceBusinessException(String code, Object... args) {
        super(ExceptionSysMarkEnum.RESOURCE_MARK, ResourceGlobalConstants.APPID, code,
                ResourceExceptionCodeEnum.getMsg(code), args);
    }

    public ResourceBusinessException(String code, Throwable cause, Object... args) {
        super(ExceptionSysMarkEnum.RESOURCE_MARK, ResourceGlobalConstants.APPID, code,
                ResourceExceptionCodeEnum.getMsg(code), cause, args);
    }

}
