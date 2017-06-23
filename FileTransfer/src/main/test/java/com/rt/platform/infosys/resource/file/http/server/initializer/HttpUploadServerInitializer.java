package com.rt.platform.infosys.resource.file.http.server.initializer;

import com.rt.platform.infosys.resourcePersistent.http.server.handler.HttpUploadServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * <pre>
 *
 * 【标题】: handler的编排类
 * 【描述】: 用来编排handler
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017/6/22 11:44
 * </pre>
 */
public class HttpUploadServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //跨域资源共享,它允许浏览器向跨源服务器，发出XMLHttpRequest请求，从而克服了AJAX只能同源使用的限制
        //CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin().build();
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpResponseEncoder());

        // Remove the following line if you don't want automatic content compression.
        pipeline.addLast(new HttpContentCompressor());

        pipeline.addLast(new HttpUploadServerHandler());
    }
}
