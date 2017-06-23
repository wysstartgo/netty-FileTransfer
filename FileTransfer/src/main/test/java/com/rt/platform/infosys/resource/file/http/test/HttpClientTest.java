package com.rt.platform.infosys.resource.file.http.test;

import com.rt.platform.infosys.resource.file.common.vo.LittleFileUploadVo;
import com.rt.platform.infosys.resource.file.http.client.HttpClientManager;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <pre>
 *
 * 【标题】:
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017/6/22 16:57
 * </pre>
 */
public class HttpClientTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientTest.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
//        for(int i=0;i<50;i++){
//            executorService.submit(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        doUpload();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        }
        doUpload();
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = HttpClientManager.getInstance().initClientPoolManager(200, 400);
        LOGGER.info(poolingHttpClientConnectionManager.getTotalStats().toString());
        Thread.sleep(1000);
        LOGGER.info(poolingHttpClientConnectionManager.getTotalStats().toString());
    }

    private static void doUpload() throws IOException {
        CloseableHttpClient httpClient = HttpClientManager.getInstance().getHttpClient();
        //上传
        LittleFileUploadVo littleFileUploadVo = new LittleFileUploadVo();

        File file = new File("F:\\tmp\\1.jpg");
        //File file = new File("F:\\spring-tool-suite-3.8.3.RELEASE-e4.6.2-win32-x86_64.zip");
        HttpEntity httpEntity = MultipartEntityBuilder.create()
                .addBinaryBody("file", file, ContentType.create("image/jpeg"), file.getName())
                .build();

        HttpPost httppost = new HttpPost("http://localhost:8080");

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
