package com.rt.platform.infosys.resource.file.common.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * <pre>
 *
 * 【标题】: 小文件上传接口响应的dto
 * 【描述】: 主要用于传输服务端向客户端响应的信息与ResultDto一起使用，放在data属性里面
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017/6/23 16:23
 * </pre>
 */
public class LittleFileUploadDto implements Serializable{

    private static final long serialVersionUID = -475430279506952928L;

    private String filePath;//上传成功后返回的路径信息

    private Long resId;//资源id

    private Date expiredDate;//过期时间

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getResId() {
        return resId;
    }

    public void setResId(Long resId) {
        this.resId = resId;
    }

    public Date getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(Date expiredDate) {
        this.expiredDate = expiredDate;
    }
}
