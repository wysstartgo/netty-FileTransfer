package com.rt.platform.infosys.resource.common.enums;

import com.rt.platform.infosys.base.common.enums.SuccessMsgEnum;

/**
 * <pre>
 *  
 *
 * 【标题】: 资源中心异常代码枚举
 * 【描述】: 
 * 【版权】: 润投科技
 * 【作者】: 唐宋  
 * 【时间】: 2017年6月22日 上午10:31:51
 * </pre>
 */
public enum ResourceExceptionCodeEnum {

    /**
     * 成功情况-默认
     */
    Ex_SUCCESS(SuccessMsgEnum.SUCCESS.getCode(), SuccessMsgEnum.SUCCESS.getMessage());

    private String code;

    private String msg;

    ResourceExceptionCodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 通过code获取msg
     * 
     * @Description
     * @author 唐宋
     * @param key
     * @return
     */
    public static String getMsg(String code) {
        for (ResourceExceptionCodeEnum exEnum : ResourceExceptionCodeEnum.values()) {
            if (exEnum.getCode().equals(code)) {
                return exEnum.getMsg();
            }
        }
        return "";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
