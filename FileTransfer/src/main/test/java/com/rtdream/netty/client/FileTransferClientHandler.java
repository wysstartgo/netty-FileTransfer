package com.rtdream.netty.client;

import com.rtdream.netty.model.RequestFile;
import com.rtdream.netty.model.ResponseFile;
import com.rtdream.netty.model.SecureModel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileTransferClientHandler extends ChannelInboundHandlerAdapter {
	private int byteRead;
	private volatile long start = 0;
	public RandomAccessFile randomAccessFile;
	//private RequestFile request;
	private final int minReadBufferSize = 81920;//8192;
	

//	public FileTransferClientHandler(RequestFile ef) {
//		if (ef.getFile().exists()) {
//			if (!ef.getFile().isFile()) {
//				System.out.println("Not a file :" + ef.getFile());
//				return;
//			}
//		}
//		this.request = ef;
//	}

	/**
	 * active status invoke
	 *
	 * @param ctx
	 */
	public void channelActive(ChannelHandlerContext ctx) {
		/*try {
			randomAccessFile = new RandomAccessFile(request.getFile(), "r");
			randomAccessFile.seek(request.getStarPos());
			byte[] bytes = new byte[minReadBufferSize];
			if ((byteRead = randomAccessFile.read(bytes)) != -1) {
				request.setEndPos(byteRead);
				request.setBytes(bytes);
				request.setFile_size(randomAccessFile.length());
				ctx.writeAndFlush(request);
			} else {
				System.out.println("文件已经读完");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException i) {
			i.printStackTrace();
		}*/
		System.out.println("channel is active!");
		SecureModel secure = new SecureModel();
		secure.setToken("2222222222222");
		ctx.writeAndFlush(secure);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		RequestFile request = GlobalContext.INSTANCE.getRequestFileMap().get("123456");
		if(msg instanceof SecureModel){
			try {
				/**
				 * 文件开始位置上传 调用方法
				 */
				randomAccessFile = new RandomAccessFile(request.getFile(), "r");
				randomAccessFile.seek(request.getStarPos()); //文件读取开始位置
				byte[] bytes = new byte[minReadBufferSize]; //最小读取文件快
				if ((byteRead = randomAccessFile.read(bytes)) != -1) { //判断文件是否读取完毕   randomAccessFile.read（）返回值
					request.setEndPos(byteRead); //标记文件当前读取结束位置，为下次读取做准备。
					request.setBytes(bytes);
					request.setFile_size(randomAccessFile.length());//设置文件总大小 ，用于服务器判断文件是否传输完成。
					ctx.writeAndFlush(request);
				} else {
					System.out.println("文件已经读完");
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException i) {
				i.printStackTrace();
			}
			return ;
		}
		/**
		 * 文件后续的读取上传方法
		 */
		if (msg instanceof ResponseFile) {
			ResponseFile response = (ResponseFile)msg;
			System.out.println(response.toString());
			if(response.isEnd()){ //判断文件上传是否完成
				randomAccessFile.close(); //关闭文件读取流
				//ctx.close();
			}else{
				start = response.getStart();
				if (start != -1) {
					randomAccessFile = new RandomAccessFile(request.getFile(), "r");
					randomAccessFile.seek(start); //移动文件读取指针到 续传位置
					int a = (int) (randomAccessFile.length() - start); //计算需要开辟的 byte[]数组空间
					int sendLength = minReadBufferSize;
					if (a < minReadBufferSize) {
						sendLength = a;
					}
					byte[] bytes = new byte[sendLength];
					if ((byteRead = randomAccessFile.read(bytes)) != -1 && (randomAccessFile.length() - start) > 0) {
						request.setEndPos(byteRead);
						request.setBytes(bytes);
						try {
							ctx.writeAndFlush(request);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						System.out.println("文件已读完!");
						randomAccessFile.close();
						ctx.close();
					}
				}
			}
		}
	}


	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

}
