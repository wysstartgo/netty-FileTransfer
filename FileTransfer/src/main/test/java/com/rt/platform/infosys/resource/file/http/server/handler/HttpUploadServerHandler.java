package com.rt.platform.infosys.resource.file.http.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

/**
 * <pre>
 *
 * 【标题】: 上传handler
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017/6/23 11:42
 * </pre>
 */
public class HttpUploadServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    // 日志
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HttpUploadServerHandler.class);

    //将request提升到全局中
    private HttpRequest request;

    private boolean readingChunks;

    //获取上传的进度信息
    private HttpData partialContent;

    //响应信息
    private final StringBuilder responseContent = new StringBuilder();

    private String resonseJsonStr = "";

    // httpDataFactory
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    //解码器
    private HttpPostRequestDecoder decoder;

    /**
     * channel建立连接时触发的方法
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (decoder != null) {
            decoder.cleanFiles();
        }
    }

    /**
     * channel读取时触发的方法
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            responseContent.setLength(0);
            responseContent.append("=========================\r\n");
            HttpRequest request = this.request = (HttpRequest) msg;
            // new getMethod
            Set<Cookie> cookies;
            String value = request.headers().get(HttpHeaderNames.COOKIE);
            if (value == null) {
                cookies = Collections.emptySet();
            } else {
                cookies = ServerCookieDecoder.STRICT.decode(value);
            }
            for (Cookie cookie : cookies) {
                responseContent.append("COOKIE: " + cookie + "\r\n");
            }
            responseContent.append("\r\n\r\n");
            QueryStringDecoder decoderQuery = new QueryStringDecoder(request.uri());
            Map<String, List<String>> uriAttributes = decoderQuery.parameters();
            for (Map.Entry<String, List<String>> attr : uriAttributes.entrySet()) {
                for (String attrVal : attr.getValue()) {
                    responseContent.append("URI: " + attr.getKey() + '=' + attrVal + "\r\n");
                }
            }
            responseContent.append("\r\n\r\n");

            // if GET Method: should not try to create a HttpPostRequestDecoder
            if (request.method().equals(HttpMethod.GET)) {
                // GET Method: should not try to create a HttpPostRequestDecoder
                // So stop here
                responseContent.append("\r\n\r\nEND OF GET CONTENT\r\n");
                // Not now: LastHttpContent will be sent
                // writeResponse(ctx.channel());
                return;
            }
            try {
                decoder = new HttpPostRequestDecoder(factory, request);
            } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                LOGGER.error("创建解码器失败!", e1);
                responseContent.append(e1.getMessage());
                writeResponse(ctx.channel());
                ctx.channel().close();
                return;
            }

            readingChunks = HttpUtil.isTransferEncodingChunked(request);
            responseContent.append("Is Chunked: " + readingChunks + "\r\n");
            responseContent.append("IsMultipart: " + decoder.isMultipart() + "\r\n");
            if (readingChunks) {
                // Chunk version
                responseContent.append("Chunks: ");
                readingChunks = true;
            }
        }

        // check if the decoder was constructed before
        // if not it handles the form get
        if (decoder != null) {
            if (msg instanceof HttpContent) {
                // New chunk is received
                HttpContent chunk = (HttpContent) msg;
                try {
                    decoder.offer(chunk);
                } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                    LOGGER.error("解码失败!",e1);
                    responseContent.append(e1.getMessage());
                    writeResponse(ctx.channel());
                    ctx.channel().close();
                    return;
                }
                responseContent.append('o');
                //通过chunk by chunk的方式读取，读取的大小由factory决定
                readHttpDataChunkByChunk();
                // example of reading only if at the end
                if (chunk instanceof LastHttpContent) {
                    //writeResponse(ctx.channel());
                    sendResponse(resonseJsonStr,ctx);
                    readingChunks = false;
                    reset();
                }
            }
        } else {
            writeResponse(ctx.channel());
        }
    }

    /**
     * 重置方法
     */
    private void reset() {
        request = null;
        // destroy the decoder to release all resources
        decoder.destroy();
        decoder = null;
    }

    /**
     * 通过chunk的方式读取数据
     */
    private void readHttpDataChunkByChunk() {
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    // check if current HttpData is a FileUpload and previously
                    // set as partial
                    if (partialContent == data) {
                        LOGGER.info(" 100% (FinalSize: " + partialContent.length() + ")");
                        partialContent = null;
                    }
                    try {
                        // new value
                        writeHttpData(data);
                    } finally {
                        data.release();
                    }
                }
            }
            // Check partial decoding for a FileUpload
            InterfaceHttpData data = decoder.currentPartialHttpData();
            if (data != null) {
                StringBuilder builder = new StringBuilder();
                if (partialContent == null) {
                    partialContent = (HttpData) data;
                    if (partialContent instanceof FileUpload) {
                        builder.append("Start FileUpload: ").append(((FileUpload) partialContent).getFilename())
                                .append(" ");
                    } else {
                        builder.append("Start Attribute: ").append(partialContent.getName()).append(" ");
                    }
                    builder.append("(DefinedSize: ").append(partialContent.definedLength()).append(")");
                }
                if (partialContent.definedLength() > 0) {
                    builder.append(" ").append(partialContent.length() * 100 / partialContent.definedLength())
                            .append("% ");
                    LOGGER.info(builder.toString());
                } else {
                    builder.append(" ").append(partialContent.length()).append(" ");
                    LOGGER.info(builder.toString());
                }
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
            // end 读取结束时
            responseContent.append("\r\n\r\nEND OF CONTENT CHUNK BY CHUNK\r\n\r\n");
        }
    }

    /**
     * 读取http data信息
     * @param data
     */
    private void writeHttpData(InterfaceHttpData data) {
        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            String value;
            try {
                value = attribute.getValue();
            } catch (IOException e1) {
                LOGGER.error("读取失败,会输出头信息!",e1);
                responseContent.append("\r\nBODY Attribute: " + attribute.getHttpDataType().name() + ": "
                        + attribute.getName() + " Error while reading value: " + e1.getMessage() + "\r\n");
                return;
            }
            if (value.length() > 100) {
                responseContent.append("\r\nBODY Attribute: " + attribute.getHttpDataType().name() + ": "
                        + attribute.getName() + " data too long\r\n");
            } else {
                responseContent.append(
                        "\r\nBODY Attribute: " + attribute.getHttpDataType().name() + ": " + attribute + "\r\n");
            }
        } else {
            responseContent.append("\r\nBODY FileUpload: " + data.getHttpDataType().name() + ": " + data + "\r\n");
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                FileUpload fileUpload = (FileUpload) data;
                if (fileUpload.isCompleted()) {
                    //将文件持久化到磁盘
                    saveFileToDisk(fileUpload);
                    if (fileUpload.length() < 10000) {
                        responseContent.append("\tContent of file\r\n");
                        try {
                            responseContent.append(fileUpload.getString(fileUpload.getCharset()));
                        } catch (IOException e1) {
                            // do nothing for the example
                            e1.printStackTrace();
                        }
                        responseContent.append("\r\n");
                    } else {
                        responseContent.append("\tFile too long to be printed out:" + fileUpload.length() + "\r\n");
                    }
                } else {
                    responseContent.append("\tFile to be continued but should not!\r\n");
                }
            }
        }
    }

    /**
     * 向客户端输出响应信息
     * @param channel
     */
    private void writeResponse(Channel channel) {
        // Convert the response content to a ChannelBuffer.
        ByteBuf buf = copiedBuffer(responseContent.toString(), CharsetUtil.UTF_8);
        responseContent.setLength(0);
        // Decide whether to close the connection or not.
        boolean close = request.headers().contains(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE, true)
                || request.protocolVersion().equals(HttpVersion.HTTP_1_0)
                        && !request.headers().contains(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE, true);
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (!close) {
            // There's no need to add 'Content-Length' header
            // if this is the last response.
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
        }

        Set<Cookie> cookies;
        String value = request.headers().get(HttpHeaderNames.COOKIE);
        if (value == null) {
            cookies = Collections.emptySet();
        } else {
            cookies = ServerCookieDecoder.STRICT.decode(value);
        }
        if (!cookies.isEmpty()) {
            // Reset the cookies if necessary.
            for (Cookie cookie : cookies) {
                response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
            }
        }
        // Write the response.
        ChannelFuture future = channel.writeAndFlush(response);
        // Close the connection after the write operation is done if necessary.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * 发送响应信息
     *
     * @param responseStr
     * @param ctx
     */
    private void sendResponse(String responseStr, ChannelHandlerContext ctx) {
        String responseString = "Unexpected error occurred";
        String contentType = "application/json; charset=UTF-8";
        HttpResponseStatus status = HttpResponseStatus.OK;
        if (responseStr != null) {
            responseString = responseStr;
        } else {
            LOGGER.info("uploaded file names are blank!");
            status = HttpResponseStatus.BAD_REQUEST;
            contentType = "text/plain; charset=UTF-8";
        }
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(responseString, CharsetUtil.UTF_8));

        response.headers().set(CONTENT_TYPE, contentType);
        response.headers().add("Access-Control-Allow-Origin", "*");

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 将文件保存到磁盘中
     *
     * @param fileUpload 要上传的文件
     * @return 文件保存的信息
     */
    private String saveFileToDisk(FileUpload fileUpload) {
        String filePath = null; // full path of the new file to be saved
        String upoadedFileName = fileUpload.getFilename();
        String extension = "";
        int i = upoadedFileName.lastIndexOf('.');
        if (i > 0) {
            // get extension including the "."
            extension = upoadedFileName.substring(i);
        }
        String uniqueBaseName = getUniqueId();
        String fileName = uniqueBaseName + extension;
        try {
            filePath = "F:\\nettyFile\\" + fileName;
            fileUpload.renameTo(new File(filePath));
            //TODO 响应信息格式化
            resonseJsonStr = filePath;
            // responseJson.put("file", fileName);
            // if (isImageExtension(extension)) {
            // String thumbname = createThumbnail(filePath, uniqueBaseName,
            // extension);
            // responseJson.put("thumb", thumbname);
            // }
        } catch (IOException ex) {
        }
        return "success";
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(responseContent.toString(), cause);
        ctx.channel().close();
    }

    /**
     * 生成一个唯一的id
     *
     * @return 返回id
     */
    private String getUniqueId() {
        //TODO md5较验及命名规范
        UUID uniqueId = UUID.randomUUID();
        return uniqueId.toString();
    }
}
