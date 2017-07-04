package com.rt.platform.infosys.resource.http.type;

/**
 * <pre>
 *
 * 【标题】: 请求类型
 * 【描述】: request type       http|https
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017/6/22 15:22
 * </pre>
 */
public enum  RequestTypeEnum {

    HTTP(1,"http"),HTTPS(2,"https");

    private int code;
    private String description;

    RequestTypeEnum(int code,String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }
}
