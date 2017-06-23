package com.rt.platform.infosys.resource.file.common.dto;

import java.io.Serializable;

/**
 * <pre>
 *
 * 【标题】: 响应信息通用的dto
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017/6/23 16:22
 * </pre>
 */
public class ResultDto implements Serializable {

    private static final long serialVersionUID = -6928256792242089560L;

    private String responseCode;// 响应码

    private String message;// 描述信息

    private String data;// 数据信息

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
