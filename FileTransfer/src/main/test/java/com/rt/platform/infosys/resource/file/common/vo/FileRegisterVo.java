package com.rt.platform.infosys.resource.file.common.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * <pre>
 *
 * 【标题】: 注删接口客户端向服务端传送的信息vo类
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017/6/23 16:31
 * </pre>
 */
public class FileRegisterVo implements Serializable{

    private static final long serialVersionUID = 1769151153082724611L;

    private Long resId;//资源唯一标识

    private Boolean whetherPermanentPersistent;//是否是永久性资源

    private Date expiredDate;//失效日期

    private String path;//文件路径

    private String token;//权限token 即authCode

    private String appId;//应用id

    public Long getResId() {
        return resId;
    }

    public void setResId(Long resId) {
        this.resId = resId;
    }

    public Boolean getWhetherPermanentPersistent() {
        return whetherPermanentPersistent;
    }

    public void setWhetherPermanentPersistent(Boolean whetherPermanentPersistent) {
        this.whetherPermanentPersistent = whetherPermanentPersistent;
    }

    public Date getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(Date expiredDate) {
        this.expiredDate = expiredDate;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
