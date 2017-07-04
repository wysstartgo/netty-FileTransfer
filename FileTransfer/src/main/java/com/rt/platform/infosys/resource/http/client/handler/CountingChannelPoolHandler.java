package com.rt.platform.infosys.resource.http.client.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <pre>
 *
 * 【标题】:
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017-06-27 10:54
 * </pre>
 */
public class CountingChannelPoolHandler implements ChannelPoolHandler {
    private final AtomicInteger channelCount = new AtomicInteger(0);

    private final AtomicInteger acquiredCount = new AtomicInteger(0);

    private final AtomicInteger releasedCount = new AtomicInteger(0);

    @Override
    public void channelCreated(Channel ch) {
        channelCount.incrementAndGet();
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("codec", new HttpClientCodec());

        // Remove the following line if you don't want automatic content decompression.
        pipeline.addLast("inflater", new HttpContentDecompressor());

        // to be used since huge file transfer
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        pipeline.addLast("httpObjectAggregator",new HttpObjectAggregator(1024000));//1M
        pipeline.addLast("handler", new HttpUploadClientHandler());
    }

    @Override
    public void channelReleased(Channel ch) {
        releasedCount.incrementAndGet();
    }

    @Override
    public void channelAcquired(Channel ch) {
        acquiredCount.incrementAndGet();
    }

    public int channelCount() {
        return channelCount.get();
    }

    public int acquiredCount() {
        return acquiredCount.get();
    }

    public int releasedCount() {
        return releasedCount.get();
    }
}