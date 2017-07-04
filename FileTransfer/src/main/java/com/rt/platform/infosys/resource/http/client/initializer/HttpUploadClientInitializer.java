package com.rt.platform.infosys.resource.http.client.initializer;

import com.rt.platform.infosys.resource.http.client.handler.HttpUploadClientHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.stream.ChunkedWriteHandler;


/**
 * <pre>
 *
 * 【标题】:
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017/6/22 19:58
 * </pre>
 */
public class HttpUploadClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("codec", new HttpClientCodec());

        // Remove the following line if you don't want automatic content decompression.
        pipeline.addLast("inflater", new HttpContentDecompressor());

        // to be used since huge file transfer
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        pipeline.addLast("httpObjectAggregator",new HttpObjectAggregator(1024000));//1M
        pipeline.addLast("handler", new HttpUploadClientHandler());

    }
}
