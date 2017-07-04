package com.rt.platform.infosys.resource.http.client;

import com.rt.platform.infosys.resource.http.client.handler.CountingChannelPoolHandler;
import com.rt.platform.infosys.resource.http.client.initializer.HttpUploadClientInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * <pre>
 *
 * 【标题】: 用netty进行http文件传输的处理类
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017-06-27 10:29
 * </pre>
 */
public class NettyHttpClientManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyHttpClientManager.class);

    //一个单例
    private static NettyHttpClientManager instance = new NettyHttpClientManager();

    //连接池对象，key是ip:port  因为可能维护着多个服务端的连接信息
    private ChannelPoolMap<InetSocketAddress,FixedChannelPool> poolChannelPoolMap = null;

    //和poolChannelPoolMap绑定的，所有对该map的写操作都要获取到这个Lock
    private final Object LOCK = new Object();

    //最大连接数
    private static final int maxConnections = 5;

    private NettyHttpClientManager(){}

    public static NettyHttpClientManager build(){
        return instance;
    }

    /**
     * 获取netty client连接的方法
     * @param host
     * @param port
     * @return
     */
    public Future<Channel> getNettyHttpClient(String host, int port){
        InetSocketAddress inetSocketAddress = new InetSocketAddress(host,port);
        if(poolChannelPoolMap == null){
            synchronized (LOCK){
                if(poolChannelPoolMap == null){
                    LOGGER.info("======================初始化channelMap=========================");
                    EventLoopGroup group = new NioEventLoopGroup();
                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.group(group).channel(NioSocketChannel.class).handler(new HttpUploadClientInitializer());
                    poolChannelPoolMap = new AbstractChannelPoolMap<InetSocketAddress, FixedChannelPool>() {
                        @Override
                        protected FixedChannelPool newPool(InetSocketAddress key) {
                            return new FixedChannelPool(bootstrap.remoteAddress(key),new CountingChannelPoolHandler(),maxConnections);
                        }
                    };
                }
            }
        }
        FixedChannelPool fixedChannelPool = poolChannelPoolMap.get(inetSocketAddress);
        Future<Channel> channelFuture = fixedChannelPool.acquire();
        //注意，此时channel context已经处于触发状态即active状态，如果重写了handler的isActive方法即会触发
        channelFuture.addListener(new FutureListener<Channel>(){

            @Override
            public void operationComplete(Future<Channel> future) throws Exception {
                if(future.isSuccess()){
                    Channel channel = future.getNow();
                    fixedChannelPool.release(channel);
                }
            }
        });
        return channelFuture;
    }
}
