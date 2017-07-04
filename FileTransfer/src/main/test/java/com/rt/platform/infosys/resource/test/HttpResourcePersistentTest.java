package com.rt.platform.infosys.resource.test;

import com.alibaba.fastjson.JSONObject;
import com.rt.platform.infosys.resource.common.dto.ResultDto;
import com.rt.platform.infosys.resource.common.util.MD5FileUtil;
import com.rt.platform.infosys.resource.common.vo.FileDeleteVo;
import com.rt.platform.infosys.resource.common.vo.FileRegisterVo;
import com.rt.platform.infosys.resource.common.vo.FileUploadVo;
import com.rt.platform.infosys.resource.http.client.ApacheHttpClientManager;
import com.rt.platform.infosys.resource.http.client.operate.impl.NettyClientOperator;
import com.rt.platform.infosys.resource.http.client.operate.impl.NormalResourceOperator;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * <pre>
 *
 * 【标题】: 测试类
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017/6/22 16:57
 * </pre>
 */
public class HttpResourcePersistentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpResourcePersistentTest.class);


    @Test
    public void testFileSize() throws IOException {
        File file = new File("F:\\tmp\\1.jpg");
        LOGGER.info(file.length() + "");
        LOGGER.info(MD5FileUtil.getFileMD5String(file));
    }

    @Test
    public void testUpload() throws Exception {
        File file = new File("F:\\tmp\\1.jpg");
        //上传
        FileUploadVo littleFileUploadVo = new FileUploadVo();
        littleFileUploadVo.setAppId("testApp");
        littleFileUploadVo.setFileSize(file.length());
        littleFileUploadVo.setFile(file);
        littleFileUploadVo.setMd5(MD5FileUtil.getFileMD5String(file));
        littleFileUploadVo.setResType(0);
        ResultDto resultDto = NormalResourceOperator.getInstance().doUpload(littleFileUploadVo, "192.168.1.8",10012);
        LOGGER.debug(JSONObject.toJSONString(resultDto));
    }

    @Test
    public void testRegister() throws Exception {
        FileRegisterVo fileRegisterVo = new FileRegisterVo();
        fileRegisterVo.setAppId("testApp");
        fileRegisterVo.setExpiredDate(new Date());
        fileRegisterVo.setWhetherPermanentPersistent(true);
        fileRegisterVo.setResId(16L);
        ResultDto resultDto = NormalResourceOperator.getInstance().doRegister(fileRegisterVo, "192.168.1.8",10012);
        LOGGER.debug(JSONObject.toJSONString(resultDto));
    }

    @Test
    public void testDelete() throws Exception {
        FileDeleteVo fileDeleteVo = new FileDeleteVo();
        fileDeleteVo.setAppId("testApp");
        fileDeleteVo.setResId(16L);
        fileDeleteVo.setToken("");
        ResultDto resultDto = NormalResourceOperator.getInstance().doDelete(fileDeleteVo, "192.168.1.8",10012);
        LOGGER.debug(JSONObject.toJSONString(resultDto));
    }

    @Test
    public void testSmallAndBigFileUpload() throws Exception{
        //File file = new File("F:\\tmp\\6.jpg");
        File file = new File("F:\\spring-tool-suite-3.8.3.RELEASE-e4.6.2-win32-x86_64.zip");
        //上传
        FileUploadVo fileUploadVo = new FileUploadVo();
        fileUploadVo.setAppId("testApp");
        fileUploadVo.setFileSize(file.length());
        fileUploadVo.setFile(file);
        fileUploadVo.setMd5(MD5FileUtil.getFileMD5String(file));
        fileUploadVo.setResType(0);
        ResultDto resultDto = NettyClientOperator.INSTANCE.doUpload(fileUploadVo, "192.168.1.8",10012);
        LOGGER.debug(JSONObject.toJSONString(resultDto));
    }

    @Test
    public void doHttpClientMultiPartUpload() throws IOException {
        CloseableHttpClient httpClient = ApacheHttpClientManager.getInstance().getHttpClient();
        File file = new File("F:\\tmp\\1.jpg");
        //上传
        FileUploadVo littleFileUploadVo = new FileUploadVo();
        littleFileUploadVo.setAppId("testApp");
        littleFileUploadVo.setFileSize(file.length());
        littleFileUploadVo.setFile(file);
        littleFileUploadVo.setMd5(MD5FileUtil.getFileMD5String(file));
        littleFileUploadVo.setResType(0);
        //File file = new File("F:\\spring-tool-suite-3.8.3.RELEASE-e4.6.2-win32-x86_64.zip");
        HttpEntity httpEntity = MultipartEntityBuilder.create()
                .addTextBody("message", JSONObject.toJSONString(littleFileUploadVo),ContentType.APPLICATION_JSON)
                .addBinaryBody("file", file, ContentType.create("image/jpeg"), file.getName())
                .build();

        HttpPost httppost = new HttpPost("http://192.168.1.8:10012");

        httppost.setEntity(httpEntity);
        System.out.println("executing request " + httppost.getRequestLine());

        CloseableHttpResponse response = httpClient.execute(httppost);

        System.out.println("----------------------------------------");
        System.out.println(response.getStatusLine());
        HttpEntity resEntity = response.getEntity();
        if (resEntity != null) {
            System.out.println("Response content length: " + resEntity.getContentLength());
        }
        System.out.println(EntityUtils.toString(resEntity));

        EntityUtils.consume(resEntity);

        response.close();
    }

    @Test
    public void doHttpClientPost() throws IOException {
        CloseableHttpClient httpClient = ApacheHttpClientManager.getInstance().getHttpClient();
        File file = new File("F:\\tmp\\1.jpg");
        //上传
        FileUploadVo littleFileUploadVo = new FileUploadVo();
        littleFileUploadVo.setAppId("testApp");
        littleFileUploadVo.setFileSize(file.length());
        littleFileUploadVo.setFile(file);
        littleFileUploadVo.setMd5(MD5FileUtil.getFileMD5String(file));
        littleFileUploadVo.setResType(0);
        //File file = new File("F:\\spring-tool-suite-3.8.3.RELEASE-e4.6.2-win32-x86_64.zip");
        HttpEntity httpEntity = MultipartEntityBuilder.create()
                .addTextBody("message", JSONObject.toJSONString(littleFileUploadVo),ContentType.APPLICATION_JSON)
                .build();

        HttpPost httppost = new HttpPost("http://192.168.1.8:10012");

        httppost.setEntity(httpEntity);
        System.out.println("executing request " + httppost.getRequestLine());

        CloseableHttpResponse response = httpClient.execute(httppost);

        System.out.println("----------------------------------------");
        System.out.println(response.getStatusLine());
        HttpEntity resEntity = response.getEntity();
        if (resEntity != null) {
            System.out.println("Response content length: " + resEntity.getContentLength());
        }
        System.out.println(EntityUtils.toString(resEntity));

        EntityUtils.consume(resEntity);

        response.close();
    }
}
