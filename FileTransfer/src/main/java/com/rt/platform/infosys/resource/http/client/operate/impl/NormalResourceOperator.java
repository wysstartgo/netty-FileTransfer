package com.rt.platform.infosys.resource.http.client.operate.impl;

import com.alibaba.fastjson.JSONObject;
import com.rt.platform.infosys.resource.common.dto.ResultDto;
import com.rt.platform.infosys.resource.common.vo.IFileBaseVo;
import com.rt.platform.infosys.resource.common.vo.FileUploadVo;
import com.rt.platform.infosys.resource.http.client.ApacheHttpClientManager;
import com.rt.platform.infosys.resource.http.client.operate.ABaseResourceOperator;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;

/**
 * <pre>
 *
 * 【标题】: 普通文件，10M以内的文件
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017-06-27 14:48
 * </pre>
 */
public class NormalResourceOperator extends ABaseResourceOperator {

    private static NormalResourceOperator normalResourceOperator = new NormalResourceOperator();

    private NormalResourceOperator() {
    }

    /**
     * 基本操作类，均以单例发布
     * 
     * @return
     */
    public static NormalResourceOperator getInstance() {
        return normalResourceOperator;
    }

    @Override
    protected ResultDto template(IFileBaseVo fileBaseVo, String host, int port) throws IOException {
        StringBuilder urlBuilder = new StringBuilder("http://").append(host).append(":").append(port);
        CloseableHttpClient httpClient = ApacheHttpClientManager.getInstance().getHttpClient();
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().addTextBody(
                fileBaseVo.getResourceCode().getCode(), JSONObject.toJSONString(fileBaseVo),
                ContentType.APPLICATION_JSON);
        switch (fileBaseVo.getResourceCode()) {
        case REOURCE_UPLOAD:
            FileUploadVo littleFileUploadVo = (FileUploadVo) fileBaseVo;
            File littleFileUploadVoFile = littleFileUploadVo.getFile();
            // 目前是图片
            multipartEntityBuilder.addBinaryBody("file", littleFileUploadVo.getFile(), ContentType.create("image/jpeg"),
                    littleFileUploadVoFile.getName());
            break;
        default:
            break;
        }
        HttpEntity httpEntity = multipartEntityBuilder.build();
        HttpPost httppost = new HttpPost(urlBuilder.toString());
        httppost.setEntity(httpEntity);
        CloseableHttpResponse response = httpClient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        ResultDto resultDto = JSONObject.parseObject(EntityUtils.toString(resEntity), ResultDto.class);
        response.close();
        return resultDto;
    }
}
