package com.rt.platform.infosys.resource.http.client.operate.impl;

import com.alibaba.fastjson.JSONObject;
import com.rt.platform.infosys.resource.common.dto.ResultDto;
import com.rt.platform.infosys.resource.common.enums.ResourceCodeEnum;
import com.rt.platform.infosys.resource.common.model.NettyHttpResponse;
import com.rt.platform.infosys.resource.common.vo.IFileBaseVo;
import com.rt.platform.infosys.resource.common.vo.FileUploadVo;
import com.rt.platform.infosys.resource.http.client.NettyHttpClientManager;
import com.rt.platform.infosys.resource.http.client.handler.HttpUploadClientHandler;
import com.rt.platform.infosys.resource.http.client.operate.ABaseResourceOperator;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * <pre>
 *
 * 【标题】: 以nettyclient的方式操作，主要针对大文件,传输部分已经完成，响应部分已经完成
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017-06-27 14:52
 * </pre>
 */
public class NettyClientOperator extends ABaseResourceOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClientOperator.class);

    public static final NettyClientOperator INSTANCE = new NettyClientOperator();

    private NettyClientOperator(){}

    @Override
    protected ResultDto template(IFileBaseVo fileBaseVo, String host, int port) throws Exception {
        Future<Channel> channelFuture = NettyHttpClientManager.build().getNettyHttpClient(host, port);
        HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); //如果超出，会存在磁盘上
        // 配置缓存的临时文件
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file
                                                         // on exit (in normal
                                                         // exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on
                                                        // exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory
        StringBuilder uriBuilder = new StringBuilder("http://").append(host).append(":").append(port)
                .append("/formpostmultipart");
        URI uriFile = new URI(uriBuilder.toString());
        return writeMultipart(channelFuture, uriFile, factory, host, fileBaseVo);
    }

    /**
     *
     * @param future
     * @param uriFile
     * @param factory
     * @param host
     * @param fileBaseVo
     * @throws Exception
     */
    private ResultDto writeMultipart(Future<Channel> future, URI uriFile, HttpDataFactory factory, String host,
            IFileBaseVo fileBaseVo) throws Exception {

        Channel channel = future.sync().get();
        DefaultPromise<NettyHttpResponse> respPromise = new DefaultPromise<NettyHttpResponse>(channel.eventLoop());
        //添加回调
        HttpUploadClientHandler httpUploadClientHandler = channel.pipeline().get(HttpUploadClientHandler.class);
        httpUploadClientHandler.setResponsePromise(respPromise);
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uriFile.toASCIIString());
        HttpPostRequestEncoder bodyRequestEncoder = new HttpPostRequestEncoder(factory, request, true); // true
        //准备头文件
        HttpHeaders headers = request.headers();
        headers.set(HttpHeaderNames.HOST, host);
        headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        headers.set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP + "," + HttpHeaderValues.DEFLATE);
        headers.set(HttpHeaderNames.ACCEPT_CHARSET, "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        headers.set(HttpHeaderNames.ACCEPT_LANGUAGE, "fr");
        headers.set(HttpHeaderNames.USER_AGENT, "Netty Simple Http Client side");
        headers.set(HttpHeaderNames.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        //添加属性
        ResourceCodeEnum resourceCode = fileBaseVo.getResourceCode();
        bodyRequestEncoder.addBodyAttribute(resourceCode.getCode(), JSONObject.toJSONString(fileBaseVo));
        switch (resourceCode){
            case REOURCE_UPLOAD:
                FileUploadVo fileUploadVo = (FileUploadVo) fileBaseVo;
                bodyRequestEncoder.addBodyFileUpload("myfile",fileUploadVo.getFile(),"application/x-zip-compressed", false);
                break;
            default:
                break;
        }
        // finalize request
        bodyRequestEncoder.finalizeRequest();
        // send request
        channel.write(request);
        // test if request was chunked and if so, finish the write
        if (bodyRequestEncoder.isChunked()) {
            channel.write(bodyRequestEncoder);
        }
        channel.flush();
        // Now no more use of file representation (and list of HttpData)
        bodyRequestEncoder.cleanFiles();
        // Wait for the server to close the connection.
        channel.closeFuture().sync();
        NettyHttpResponse response = respPromise.get();
        String responseStr = "";
        if (response != null){
            responseStr = new String(response.body());
            LOGGER.info("The client received echo response, the body is :" + responseStr);
        }
        ResultDto resultDto = JSONObject.parseObject(responseStr, ResultDto.class);
        return resultDto;
    }
}
