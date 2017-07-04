package com.rtdream.netty.client;

import com.rtdream.netty.code.NettyMessageDecoder;
import com.rtdream.netty.code.NettyMessageEncoder;
import com.rtdream.netty.model.RequestFile;
import com.rtdream.netty.util.MD5FileUtil;
import com.rtdream.nettyhttp.client.CountingChannelPoolHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

public class FileTransferClientPoolTest {

	//key为目标host，value为目标host的连接池
	public ChannelPoolMap<InetSocketAddress, FixedChannelPool> poolMap = null;

	public void init(RequestFile echoFile) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap();
		b.group(group).channel(NioSocketChannel.class).
				option(ChannelOption.TCP_NODELAY, true);

		poolMap = new AbstractChannelPoolMap<InetSocketAddress, FixedChannelPool>() {
			@Override
			protected FixedChannelPool newPool(InetSocketAddress key) {
				return new FixedChannelPool(b.remoteAddress(key), new ChannelPoolHandler() {
					public void channelReleased(Channel ch) throws Exception {
						System.out.println("22");
					}

					public void channelAcquired(Channel ch) throws Exception {
						System.out.println("33");
					}

					public void channelCreated(Channel ch) throws Exception {
						System.out.println("create");
						//可以在此绑定channel的handler
						ch.pipeline().addLast(new ObjectEncoder());
						//ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
						//ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
						ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(null))); // 最大长度
						ch.pipeline().addLast(new NettyMessageDecoder());//设置服务器端的编码和解码
						ch.pipeline().addLast(new NettyMessageEncoder());
						ch.pipeline().addLast(new FileTransferClientHandler(echoFile));
					}
				},2);//单个host连接池大小
			}
		};
	}

	public void send(InetSocketAddress address,final RequestFile msg) throws ExecutionException, InterruptedException {
		if(address == null){
			throw new RuntimeException("InetSocketAddress can not be null");
		}

		final FixedChannelPool pool = this.poolMap.get(address);

		Future<Channel> future = pool.acquire();
		GlobalContext.INSTANCE.getRequestFileMap().put("123456",msg);
//		ChannelPipeline channelPipeline = future.get().pipeline().addLast("aaa",new FileTransferClientHandler(msg));
//		channelPipeline.addLast(new NettyMessageDecoder());
//		channelPipeline.addLast(new NettyMessageEncoder());
//		for(String str:channelPipeline.names()){
//			System.out.println(str);
//		}
//		future.get().pipeline().remove(FileTransferClientHandler.class);
		future.addListener(new FutureListener<Channel>() {
			//这里在连接建立之后就会触发
			public void operationComplete(Future<Channel> f) {
				if (f.isSuccess()) {
					Channel ch = f.getNow();
//					GlobalContext.INSTANCE.getRequestFileMap().remove("123456");
					//把handler移除掉
//					ch.pipeline().remove(FileTransferClientHandler.class);
//					ch.pipeline().addLast(new FileTransferClientHandler(msg));
//					ChannelFuture lastWriteFuture = null;
//
//					lastWriteFuture = ch.writeAndFlush(msg);
//
//					// Wait until all messages are flushed before closing the channel.
//					if (lastWriteFuture != null) {
//
//						try {
//							lastWriteFuture.sync();
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
					System.out.println("success==========zzzzzzzzzzzzzzzzzzz=========" + ch.toString());
					pool.release(ch);
				}
			}
		});
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
			FileTransferClientPoolTest fileTransferClientPoolTest = new FileTransferClientPoolTest();
			fileTransferClientPoolTest.init(echo);
			fileTransferClientPoolTest.send(new InetSocketAddress("127.0.0.1",port),echo);

//			RequestFile echo2 = new RequestFile();
//			//File file = new File("F:\\spring-tool-suite-3.8.3.RELEASE-e4.6.2-win32-x86_64.zip");  //  "D://files/xxoo"+args[0]+".amr"
//			File file2 = new File("F:\\tmp\\2.jpg");  //  "D://files/xxoo"+args[0]+".amr"
//			String fileName2 = file.getName();// 文件名
//			echo.setFile(file2);
//			echo.setFile_md5(MD5FileUtil.getFileMD5String(file2));
//			echo.setFile_name(fileName2);
//			echo.setFile_type(getSuffix(fileName2));
//			echo.setStarPos(0);// 文件开始位置
//			fileTransferClientPoolTest.init(echo2);
//			fileTransferClientPoolTest.send(new InetSocketAddress("127.0.0.1",port),echo2);



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
