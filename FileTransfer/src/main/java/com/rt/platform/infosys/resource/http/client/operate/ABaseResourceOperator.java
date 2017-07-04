package com.rt.platform.infosys.resource.http.client.operate;

import com.rt.platform.infosys.resource.common.dto.ResultDto;
import com.rt.platform.infosys.resource.common.vo.IFileBaseVo;
import com.rt.platform.infosys.resource.common.vo.FileDeleteVo;
import com.rt.platform.infosys.resource.common.vo.FileRegisterVo;
import com.rt.platform.infosys.resource.common.vo.FileUploadVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <pre>
 *
 * 【标题】: 抽象操作类
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017-06-27 14:45
 * </pre>
 */
public abstract class ABaseResourceOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ABaseResourceOperator.class);

    /**
     * 进行注册操作
     *
     * @param fileRegisterVo
     * @param host
     * @param port
     * @return
     */
    public ResultDto doRegister(FileRegisterVo fileRegisterVo, String host, int port) throws Exception {
        // TODO 注册的专属较验
        return template(fileRegisterVo, host, port);
    }

    /**
     * 上传操作
     *
     * @param littleFileUploadVo
     * @param host
     * @param port
     * @return
     * @throws IOException
     */
    public ResultDto doUpload(FileUploadVo littleFileUploadVo, String host, int port) throws Exception {
        // TODO 上传的专属较验部分
        return template(littleFileUploadVo, host, port);
    }

    /**
     * 删除操作
     *
     * @param fileDeleteVo
     * @param host
     * @param port
     * @return
     * @throws IOException
     */
    public ResultDto doDelete(FileDeleteVo fileDeleteVo, String host, int port) throws Exception {
        // TODO 删除的专属校验部分
        return template(fileDeleteVo, host, port);
    }

    /**
     * 供子类实现
     * 
     * @param fileBaseVo
     * @param host
     * @param port
     * @return
     * @throws IOException
     */
    protected abstract ResultDto template(IFileBaseVo fileBaseVo, String host, int port) throws Exception;
}
