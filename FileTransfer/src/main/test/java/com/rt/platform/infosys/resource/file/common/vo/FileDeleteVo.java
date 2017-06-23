package com.rt.platform.infosys.resource.file.common.vo;

import java.io.Serializable;

/**
 * <pre>
 *
 * 【标题】: 客户端向服务端发送删除文件请求时对应的vo
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017/6/23 16:37
 * </pre>
 */
public class FileDeleteVo implements Serializable {

    private static final long serialVersionUID = 9178937160099012486L;

    private Long resId;// 资源id

    private String token;// 即授权码 authCode

    private String appId;// 应用的id

    public Long getResId() {
        return resId;
    }

    public void setResId(Long resId) {
        this.resId = resId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
