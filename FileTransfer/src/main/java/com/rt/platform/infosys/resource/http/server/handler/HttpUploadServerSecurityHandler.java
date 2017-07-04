package com.rt.platform.infosys.resource.http.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 *
 * 【标题】: 权限控制handler
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017-06-26 10:34
 * </pre>
 */
public class HttpUploadServerSecurityHandler extends ChannelInboundHandlerAdapter{

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUploadServerSecurityHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.info("getRead===============");
        super.channelRead(ctx, msg);
    }
}
