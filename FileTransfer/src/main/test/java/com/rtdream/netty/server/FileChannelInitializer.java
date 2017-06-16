package com.rtdream.netty.server;

import com.rtdream.netty.code.NettyMessageDecoder;
import com.rtdream.netty.code.NettyMessageEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class FileChannelInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(new ObjectEncoder());
      //  ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
        //ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
        ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(null))); // 最大长度

        ch.pipeline().addLast(new NettyMessageDecoder());//设置服务器端的编码和解码
        ch.pipeline().addLast(new NettyMessageEncoder());

        ch.pipeline().addLast(new SecureServerHandler());
        ch.pipeline().addLast(new FileTransferServerHandler());
    }

}
