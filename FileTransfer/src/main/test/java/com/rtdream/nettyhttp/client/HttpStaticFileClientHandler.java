package com.rtdream.nettyhttp.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * Created by wuys on 2017/6/14.
 *
 * @author wuys
 * @date 2017/6/14
 */
public class HttpStaticFileClientHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        File file = new File("F:\\tmp\\1.jpg");
        RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r");
        byte[] bytes = new byte[1024];
        long byteRead = 0;
        if((byteRead = randomAccessFile.read(bytes)) != -1 && randomAccessFile.length() > 0){
            ByteBuf byteBuf = Unpooled.copiedBuffer(bytes);
            DefaultFullHttpRequest defaultFullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                    "http://localhost:8080",byteBuf);
            System.out.println("================");
            ctx.writeAndFlush(defaultFullHttpRequest);
        }
    }
}
