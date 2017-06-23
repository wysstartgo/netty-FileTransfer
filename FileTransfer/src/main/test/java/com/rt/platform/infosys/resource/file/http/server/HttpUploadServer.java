package com.rt.platform.infosys.resource.file.http.server;

import com.rt.platform.infosys.resourcePersistent.common.model.PropertiesFile;
import com.rt.platform.infosys.resourcePersistent.http.server.initializer.HttpUploadServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

/**
 * <pre>
 *
 * 【标题】: netty传输文件的server
 * 【描述】: 用来接收客户端的传输请求，并进行文件处理的逻辑
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017/6/21 17:15
 * </pre>
 */
public final class HttpUploadServer {

    // 日志
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUploadServer.class);

    // 一个server单例
    private static final HttpUploadServer server = new HttpUploadServer();

    // 配置信息
    private volatile PropertiesFile propertiesFile = null;

    private HttpUploadServer() {
    }

    /**
     * 获取到配置信息，一般在该入口类实例化之后就会创建 主要供其他类使用，针对这个实例来说是只读的，也是线程安全的
     * 
     * @return propertiesFile
     */
    public static PropertiesFile getPropertiesFile() {
        return server.propertiesFile;
    }

    /**
     * 执行一些初始化的操作
     */
    private void init() {
        // 请把加载属性文件放在 加载日志配置的上面，因为读取日志输出的目录配置在 属性文件里面
        propertiesFile = PropertiesFile.buildInstance("resource.properties");
        try {
            URL url = HttpUploadServer.class.getClassLoader().getResource("log4j2.xml");
            ConfigurationSource configurationSource = new ConfigurationSource(url.openStream());
            Configurator.initialize(null, configurationSource);
        } catch (IOException e) {
            LOGGER.error("初始化异常！", e);
        }
    }

    /**
     * 启动一个serverBootstrap并与指定端口绑定
     * 
     * @param port
     *            端口
     * @throws Exception
     */
    private void bind(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    // BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new HttpUploadServerInitializer());

            ChannelFuture f = b.bind(port).sync();
            LOGGER.info("文件服务器启动成功！端口号：" + port);
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            LOGGER.error("server启动异常!有可能是端口被占用或其他问题引起的。", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * main方法，在里面执行主要逻辑
     * @param args
     */
    public static void main(String[] args) {
        // 执行初始化
        server.init();
        // 获取端口号
        Integer port = server.propertiesFile.getInteger("port", 10020);
        // 执行绑定
        server.bind(port);
    }

}
