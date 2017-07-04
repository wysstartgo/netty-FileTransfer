package com.rt.platform.infosys.resource.http.exception;

/**
 * <pre>
 *
 * 【标题】: 超类exception
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017-06-30 16:24
 * </pre>
 */
public class ResourceException extends Exception {

    private static final long serialVersionUID = 1900498705324708624L;

    private String code;

    private String msg;

    public ResourceException(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
