package com.rtdream.netty.code;

import com.rtdream.netty.util.ObjectConvertUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * 编码器
 * @author 洋白菜
 *
 */
public class NettyMessageEncoder extends MessageToMessageEncoder<Object> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg,
                          List<Object> out) throws Exception {
		out.add(ObjectConvertUtil.request(msg));
	}

}
