package com.rtdream.nettyhttp.client;

import com.rtdream.netty.client.FileTransferClientHandler;
import com.rtdream.netty.code.NettyMessageDecoder;
import com.rtdream.netty.code.NettyMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by wuys on 2017/6/14.
 *
 * @author wuys
 * @date 2017/6/14
 */
public class NettyClient {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).
                option(ChannelOption.SO_KEEPALIVE,true).remoteAddress(new InetSocketAddress("127.0.0.1",8080))
                .handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new ObjectEncoder());
                ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(null))); // 最大长度
                ch.pipeline().addLast(new NettyMessageDecoder());//设置服务器端的编码和解码
                ch.pipeline().addLast(new NettyMessageEncoder());
                ch.pipeline().addLast(new HttpStaticFileClientHandler());
            }
        });
        //ChannelPool pool = new SimpleChannelPool(bootstrap, new CountingChannelPoolHandler());
        FixedChannelPool pool = new FixedChannelPool(bootstrap, new CountingChannelPoolHandler(),
                1, 10);
        Channel channel = pool.acquire().sync().getNow();
        pool.release(channel);
//        bootstrap.connect();
//        Channel channel = pool.acquire().sync().getNow();

//        channel.closeFuture().sync();
    }

}
