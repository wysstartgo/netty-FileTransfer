package com.rt.platform.infosys.resource.file.common.vo;

import java.io.File;
import java.io.Serializable;

/**
 * <pre>
 *
 * 【标题】: 小文件上传接口对应的vo文件
 * 【描述】: 客户端向服务器发送文件上传请求时的vo
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017/6/23 16:17
 * </pre>
 */
public class LittleFileUploadVo implements Serializable{

    private static final long serialVersionUID = 6229682882317267581L;

    private String token;//身份认证信息

    private Integer resType;//资源类型

    private String md5;//文件md5

    private Long fileSize;//文件大小

    private String appId;//应用id

    private transient File file;//文件,不序列化

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getResType() {
        return resType;
    }

    public void setResType(Integer resType) {
        this.resType = resType;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
