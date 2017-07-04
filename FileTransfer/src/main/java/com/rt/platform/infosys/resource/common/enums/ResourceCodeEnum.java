package com.rt.platform.infosys.resource.common.enums;

/**
 * <pre>
 *
 * 【标题】: 资源上传、注册、删除接口的枚举类
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017-06-26 15:38
 * </pre>
 */
public enum ResourceCodeEnum {

    REOURCE_UPLOAD("01","资源上传"),RSOURCE_REGISTER("02","资源注册"),RESOURCE_DELETE("03","资源删除");

    private String code;

    private String description;

    private ResourceCodeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
