package com.rt.platform.infosys.resource.http.validater;

import com.rt.platform.infosys.resource.common.vo.FileDeleteVo;
import com.rt.platform.infosys.resource.common.vo.FileRegisterVo;
import com.rt.platform.infosys.resource.common.vo.FileUploadVo;

/**
 * <pre>
 *
 * 【标题】: 对资源上传，注册和删除接口的参数进行校验
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017-06-30 16:40
 * </pre>
 */
public class ResourceVoParameterValidater {

    public static final ResourceVoParameterValidater INSTANCE = new ResourceVoParameterValidater();

    private ResourceVoParameterValidater(){}

    /**
     * 校验上传
     * @param fileUploadVo
     * @return
     */
    public boolean validateFileUploadVo(FileUploadVo fileUploadVo){
        //目前主要是做非空校验
        if(fileUploadVo.getAppId() == null || fileUploadVo.getFileSize() == null || fileUploadVo.getIsFreeCharge() == null
            || fileUploadVo.getMd5() == null || fileUploadVo.getResourceCode() == null || fileUploadVo.getResType() == null
            || fileUploadVo.getToken() == null){
            return false;
        }
        return true;
    }

    /**
     * 校验注册
     * @param fileRegisterVo
     * @return
     */
    public boolean validateFileRegisterVo(FileRegisterVo fileRegisterVo){
        if(fileRegisterVo.getWhetherPermanentPersistent() == null || fileRegisterVo.getAppId() == null
                || fileRegisterVo.getExpiredDate() == null || fileRegisterVo.getIsFreeCharge() == null
                || fileRegisterVo.getResId() == null || fileRegisterVo.getResourceCode() == null
                || fileRegisterVo.getToken() == null){
            return false;
        }
        return true;
    }

    /**
     * 删除校验
     * @param fileDeleteVo
     * @return
     */
    public boolean validateFileDeleteVo(FileDeleteVo fileDeleteVo){
        if(fileDeleteVo.getAppId() == null || fileDeleteVo.getResId() == null || fileDeleteVo.getResourceCode() == null
                || fileDeleteVo.getToken() == null){
            return false;
        }
        return true;
    }


}
