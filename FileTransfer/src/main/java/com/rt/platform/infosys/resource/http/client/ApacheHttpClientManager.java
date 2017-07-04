package com.rt.platform.infosys.resource.http.client;

import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

/**
 * <pre>
 *
 * 【标题】: httpclient的工具类,这个是用apache的httpclient实现的
 * 【描述】: 用来管理httpclient连接,适合小文件的传输处理
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017/6/22 15:17
 * </pre>
 */
public class ApacheHttpClientManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApacheHttpClientManager.class);

    // 连接池管理器
    private volatile PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = null;

    private static final ApacheHttpClientManager httpclientManager = new ApacheHttpClientManager();

    // 请求处理器
    private HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            if (executionCount >= 3) {// 如果已经重试了3次，就放弃
                return false;
            }
            if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                return true;
            }
            if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                return false;
            }
            if (exception instanceof InterruptedIOException) {// 超时
                return false;
            }
            if (exception instanceof UnknownHostException) {// 目标服务器不可达
                return false;
            }
            if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                return false;
            }
            if (exception instanceof SSLException) {// SSL握手异常
                return false;
            }

            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            // 如果请求是幂等的，就再次尝试
            if (!(request instanceof HttpEntityEnclosingRequest)) {
                return true;
            }
            return false;
        }
    };

    // 连接存活策略
    ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {

        public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            // Honor 'keep-alive' header
            HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    try {
                        return Long.parseLong(value) * 1000;
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
            HttpHost target = (HttpHost) context.getAttribute(HttpClientContext.HTTP_TARGET_HOST);
            if ("www.naughty-server.com".equalsIgnoreCase(target.getHostName())) {
                // Keep alive for 5 seconds only
                return 5 * 1000;
            } else {
                // otherwise keep alive for 30 seconds
                return 30 * 1000;
            }
        }

    };

    // 一个常量对象
    private static final Object LOCK = new Object();

    private ApacheHttpClientManager() {
    }

    public static ApacheHttpClientManager getInstance() {
        return httpclientManager;
    }

    /**
     *
     * @param maxTotal
     *            最大连接数
     * @param maxPerRoute
     *            每个路由的最大连接数
     * @param maxRoute
     *            主机的最大路由数
     * @param hostname
     *            主机名或ip地址
     * @param port
     *            端口
     * @return 连接池管理器
     */
    private PoolingHttpClientConnectionManager getHttpClientPoolManager(int maxTotal, int maxPerRoute, int maxRoute,
            String hostname, int port) {
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register("http", plainsf).register("https", sslsf).build();
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(
                registry);
        // 将最大连接数增加
        poolingHttpClientConnectionManager.setMaxTotal(maxTotal);
        // 将每个路由基础的连接增加
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(maxPerRoute);
        HttpHost httpHost = new HttpHost(hostname, port);
        // 将目标主机的最大连接数增加
        poolingHttpClientConnectionManager.setMaxPerRoute(new HttpRoute(httpHost), maxRoute);
        return poolingHttpClientConnectionManager;
    }

    /**
     * 初始化连接池管理器
     *
     * 此处解释下MaxtTotal和DefaultMaxPerRoute的区别： 1、MaxtTotal是整个池子的大小；
     * 2、DefaultMaxPerRoute是根据连接到的主机对MaxTotal的一个细分；比如： MaxtTotal=400
     * DefaultMaxPerRoute=200 而我只连接到http://sishuok.com时，到这个主机的并发最多只有200；而不是400；
     * 而我连接到http://sishuok.com 和
     * http://qq.com时，到每个主机的并发最多只有200；即加起来是400（但不能超过400）；所以起作用的设置是DefaultMaxPerRoute。
     * 
     * @see http://jinnianshilongnian.iteye.com/blog/2089792#comments
     * @param maxTotal
     * @param maxPerRoute
     * @return PoolingHttpClientConnectionManager
     */
    public PoolingHttpClientConnectionManager initClientPoolManager(int maxTotal, int maxPerRoute) {
        if (poolingHttpClientConnectionManager == null) {
            synchronized (LOCK) {
                if (poolingHttpClientConnectionManager == null) {
                    ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
                    LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory();
                    Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
                            .register("http", plainsf).register("https", sslsf).build();
                    poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(registry);
                    // 将最大连接数增加
                    poolingHttpClientConnectionManager.setMaxTotal(maxTotal);
                    // 将每个路由基础的连接增加
                    poolingHttpClientConnectionManager.setDefaultMaxPerRoute(maxPerRoute);
                    // HttpHost httpHost = new HttpHost("localhost", 10012);
                    // 将目标主机的最大连接数增加
                    // poolingHttpClientConnectionManager.setMaxPerRoute(new
                    // HttpRoute(httpHost), 50);
                }
            }
        }
        return poolingHttpClientConnectionManager;
    }

    /**
     * 获取httpclient
     * 
     * @see RequestConfig#getConnectionRequestTimeout()
     * @see RequestConfig#getConnectTimeout()
     * @see RequestConfig#getSocketTimeout()
     * @return httpClient
     */
    public CloseableHttpClient getHttpClient() {
        int CONNECTION_TIMEOUT = 2 * 1000; // 设置请求超时2秒钟 根据业务调整
        int SO_TIMEOUT = 90 * 1000; // 设置等待数据超时时间10秒钟 根据业务调整
        // 定义了当从ClientConnectionManager中检索ManagedClientConnection实例时使用的毫秒级的超时时间
        int CONN_MANAGER_TIMEOUT = 500; // 该值就是连接不够用的时候等待超时时间，一定要设置，而且不能太大 ()
        // @see RequestConfig#getConnectionRequestTimeout()
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONN_MANAGER_TIMEOUT)
                .setConnectTimeout(CONNECTION_TIMEOUT).setSocketTimeout(SO_TIMEOUT).build();
        // 初始化连接池管理器
        initClientPoolManager(400, 20);
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(poolingHttpClientConnectionManager)
                .setRetryHandler(httpRequestRetryHandler)// 共用一个请求重试处理器
                .setDefaultRequestConfig(requestConfig).setKeepAliveStrategy(myStrategy) // 相同的连接存活策略
                .build();
        if (poolingHttpClientConnectionManager != null && poolingHttpClientConnectionManager.getTotalStats() != null) {
            LOGGER.info("now client pool " + poolingHttpClientConnectionManager.getTotalStats().toString());
        }
        return httpClient;
    }

}
