package com.rt.platform.infosys.resource.http.tasks;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 *
 * 【标题】: 官方给的一个用来清除httpClient pool中无用连接的线程
 * 【描述】: 清除httpclient pool中无用连接;暂未使用
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017/6/22 16:41
 * </pre>
 */
public class IdleConnectionMonitorThread extends Thread{

    private static final Logger LOGGER = LoggerFactory.getLogger(IdleConnectionMonitorThread.class);

    // PoolingHttpClientConnectionManager is a more complex implementation that
    // manages a pool
    // of client connections and is able to service connection requests from
    // multiple execution threads.
    private final PoolingHttpClientConnectionManager connMgr;

    private volatile boolean shutdown;

    public IdleConnectionMonitorThread(PoolingHttpClientConnectionManager connMgr) {
        super("IdleConnectionMonitorThread");
        this.connMgr = connMgr;
    }

    @Override
    public void run() {
        try {
            while (!shutdown) {
                synchronized (this) {
                    wait(5000);
                    // Close expired connections，停止过期的连接
                    connMgr.closeExpiredConnections();
                    // Optionally, close connections that have been idle longer
                    // than 30 sec
                    connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                }
            }
        } catch (InterruptedException ex) {
            // terminate
            LOGGER.error("IdleConnectionMonitorThread was interrupted!",ex);
        }
    }

    public void shutdown() {
        synchronized (this) {
            shutdown = true;
            notifyAll(); // 让run方法不再wait
        }
    }
}
