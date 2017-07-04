package com.rt.platform.infosys.resource.common.model;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;

/**
 * <pre>
 *
 * 【标题】:
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017-06-29 14:18
 * </pre>
 */
public class NettyHttpResponse {

    private FullHttpResponse response;

    private HttpHeaders headers;

    private byte[] body;

    public NettyHttpResponse(FullHttpResponse response){
        this.headers = response.headers();
        this.response = response;
        if (response.content() != null)
        {
            body = new byte[response.content().readableBytes()];
            response.content().getBytes(0, body);
        }
    }

    public byte[] body()
    {
//		return body = response.content() != null ?
//				response.content().array() : null;
//		if (response.content() == null)
//			return null;
//		body = new byte[response.content().readableBytes()];
//		response.content().getBytes(0, body);
        return body;
    }


}
