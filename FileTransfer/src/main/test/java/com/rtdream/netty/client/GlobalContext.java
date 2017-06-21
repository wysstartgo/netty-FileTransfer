package com.rtdream.netty.client;

import com.rtdream.netty.model.RequestFile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <pre>
 *
 * 【标题】:
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017/6/21 16:16
 * </pre>
 */
public enum GlobalContext {
    INSTANCE;

    private Map<String,RequestFile> requestFileMap = new ConcurrentHashMap<>();

    public Map<String, RequestFile> getRequestFileMap() {
        return requestFileMap;
    }
}
