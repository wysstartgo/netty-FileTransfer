package com.rt.platform.infosys.resource.http.client.handler;

import com.rt.platform.infosys.resource.common.model.NettyHttpResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.concurrent.Promise;

/**
 * <pre>
 *
 * 【标题】: HttpUploadClientHandler
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017/6/22 19:59
 * </pre>
 */
public class HttpUploadClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private boolean readingChunks;

    private Promise<NettyHttpResponse> responsePromise;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        if(this.responsePromise != null){
            this.responsePromise.setSuccess(new NettyHttpResponse(msg));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.channel().close();
    }

    public void setResponsePromise(Promise<NettyHttpResponse> responsePromise) {
        this.responsePromise = responsePromise;
    }
}
