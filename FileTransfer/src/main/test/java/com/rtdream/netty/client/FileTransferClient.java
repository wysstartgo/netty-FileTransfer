package com.rtdream.netty.client;

import com.rtdream.netty.code.NettyMessageDecoder;
import com.rtdream.netty.code.NettyMessageEncoder;
import com.rtdream.netty.model.RequestFile;
import com.rtdream.netty.util.MD5FileUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;

public class FileTransferClient {
	public void connect(int port, String host, final RequestFile echoFile) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).
					option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel ch) throws Exception {
					ch.pipeline().addLast(new ObjectEncoder());
					//ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
					//ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
					ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(null))); // 最大长度
					ch.pipeline().addLast(new NettyMessageDecoder());//设置服务器端的编码和解码
					ch.pipeline().addLast(new NettyMessageEncoder());
					//ch.pipeline().addLast(new FileTransferClientHandler(echoFile));
				}
			});

			ChannelFuture f = b.connect(host, port).sync();
			f.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully();
		}
	}
 
	public static void main(String[] args) {
		int port = 10012;
		/*if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}*/
		try {
			RequestFile echo = new RequestFile();
			//File file = new File("F:\\spring-tool-suite-3.8.3.RELEASE-e4.6.2-win32-x86_64.zip");  //  "D://files/xxoo"+args[0]+".amr"
			File file = new File("F:\\tmp\\1.jpg");  //  "D://files/xxoo"+args[0]+".amr"
			String fileName = file.getName();// 文件名
			echo.setFile(file);
			echo.setFile_md5(MD5FileUtil.getFileMD5String(file));
			echo.setFile_name(fileName);
			echo.setFile_type(getSuffix(fileName));
			echo.setStarPos(0);// 文件开始位置
			new FileTransferClient().connect(port, "127.0.0.1", echo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private static String getSuffix(String fileName)
    {
        String fileType = fileName.substring(fileName.lastIndexOf("."), fileName.length());
        return fileType;
    }
}
