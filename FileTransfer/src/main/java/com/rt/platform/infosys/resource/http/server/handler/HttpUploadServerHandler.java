package com.rt.platform.infosys.resource.http.server.handler;

import com.alibaba.fastjson.JSONObject;
import com.rt.platform.infosys.data.module.business.resource.persistence.dao.dto.InformationResourceDto;
import com.rt.platform.infosys.data.module.business.resource.persistence.dubbo.service.IDubboInformationResourceService;
import com.rt.platform.infosys.resource.common.constants.ResourceGlobalConstants;
import com.rt.platform.infosys.resource.common.dto.FileUploadDto;
import com.rt.platform.infosys.resource.common.dto.ResultDto;
import com.rt.platform.infosys.resource.common.enums.ResourceCodeEnum;
import com.rt.platform.infosys.resource.common.enums.ResourceTypeEnum;
import com.rt.platform.infosys.resource.common.util.DateFormatUtil;
import com.rt.platform.infosys.resource.common.vo.FileDeleteVo;
import com.rt.platform.infosys.resource.common.vo.FileRegisterVo;
import com.rt.platform.infosys.resource.common.vo.FileUploadVo;
import com.rt.platform.infosys.resource.common.vo.IFileBaseVo;
import com.rt.platform.infosys.resource.http.exception.ParameterInValidateException;
import com.rt.platform.infosys.resource.http.exception.ResourceException;
import com.rt.platform.infosys.resource.http.server.HttpUploadServer;
import com.rt.platform.infosys.resource.http.validater.ResourceVoParameterValidater;
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
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

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

    // 将request提升到全局中
    private HttpRequest request;

    private boolean readingChunks;

    // 获取上传的进度信息
    private HttpData partialContent;

    // 响应信息
    private final StringBuilder responseContent = new StringBuilder();

    private ResultDto resultDto = null;

    // private String resonseJsonStr = "";

    // httpDataFactory
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    // 解码器
    private HttpPostRequestDecoder decoder;

    // 建议是为dubbo专门提供一套service,因为这种直接db层面的service可能要依赖父service等
    private IDubboInformationResourceService informationResourceService = HttpUploadServer.getApplicationContext()
            .getBean(IDubboInformationResourceService.class);

    // 从客户端传入的vo对象
    private IFileBaseVo fileBaseVo;

    /**
     * channel建立连接时触发的方法
     *
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
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
            // 如果是GET方法就不需要创建 HttpPostRequestDecoder
            responseContent.append("====================\r\n");
            if (request.method().equals(HttpMethod.GET)) {
                responseContent.append("\r\n\r\nEND OF GET CONTENT\r\n");
                // 这里不会返回，因为处理的是trunk请求，所以会有LastHttpContent
                // 传递过来，然后调用sendResponse(ctx.channel())进行返回;
                // 详见http chunk协议
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

        // 检查下decoder是否已经创建，如果没有创建则表明处理的是form get请求
        if (decoder != null) {
            if (msg instanceof HttpContent) {
                // 接收到了一个新的chunk
                HttpContent chunk = (HttpContent) msg;
                try {
                    decoder.offer(chunk);
                } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                    LOGGER.error("解码失败!", e1);
                    responseContent.append(e1.getMessage());
                    writeResponse(ctx.channel());
                    ctx.channel().close();
                    return;
                }
                responseContent.append('o');
                // 通过chunk by chunk的方式读取，读取的大小由factory决定
                readHttpDataChunkByChunk(ctx);
                // 在chunk读取结束时会传来LastHttpContent
                if (chunk instanceof LastHttpContent) {
                    sendResponse(ctx);
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
    private void readHttpDataChunkByChunk(ChannelHandlerContext ctx) {
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    try {
                        // new value
                        writeHttpData(ctx, data);
                    } finally {
                        data.release();
                    }
                }
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
            // end 读取结束时 正常
            responseContent.append("\r\n\r\nEND OF CONTENT CHUNK BY CHUNK\r\n\r\n");
        }
    }

    /**
     * 读取http data信息
     *
     * @param data
     */
    private void writeHttpData(ChannelHandlerContext ctx, InterfaceHttpData data) {
        // 读取body中属性信息
        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
            // 这里在每次请求中只会调用一次
            Attribute attribute = (Attribute) data;
            String value;
            ResultDto resultDto = null;
            try {
                String name = attribute.getName();
                value = attribute.getValue();
                // TODO 校验部分,对传入参数的校验部分
                if (ResourceCodeEnum.REOURCE_UPLOAD.getCode().equals(name)) {
                    try {
                        fileBaseVo = JSONObject.parseObject(value, FileUploadVo.class);
                        FileUploadVo fileUploadVo = (FileUploadVo) fileBaseVo;
                        boolean validateFileUploadVo = ResourceVoParameterValidater.INSTANCE
                                .validateFileUploadVo(fileUploadVo);
                        if (!validateFileUploadVo) {
                            throw new ParameterInValidateException("上传文件时的参数内容不能为null!");
                        }
                        InformationResourceDto informationResourceDto = informationResourceService
                                .selectForResourceUpload(fileUploadVo.getMd5(), fileUploadVo.getFileSize());
                        if (informationResourceDto != null) {
                            FileUploadDto fileUploadDto = new FileUploadDto();
                            fileUploadDto.setExpiredDate(informationResourceDto.getExpiredDate());
                            fileUploadDto.setFilePath(informationResourceDto.getResUrl());
                            fileUploadDto.setResId(informationResourceDto.getId());
                            resultDto = new ResultDto();
                            resultDto.setMessage("该资源已经存在!");
                            resultDto.setResponseCode(ResourceGlobalConstants.RESOURCE_EXIST_CODE);
                            resultDto.setData(JSONObject.toJSONString(fileUploadDto));
                        }
                    } catch (ResourceException e) {
                        LOGGER.error("上传异常", e);
                        resultDto = buildErrorResultDto(e.getCode(), e.getMsg());
                    } catch (Exception e) {
                        LOGGER.error("上传异常", e);
                        resultDto = buildErrorResultDto(ResourceGlobalConstants.RPC_EXCEPTION_CODE,
                                "资源上传时出现服务器异常! " + e.getMessage());
                    }
                } else if (ResourceCodeEnum.RSOURCE_REGISTER.getCode().equals(name)) {
                    try {
                        FileRegisterVo fileRegisterVo = JSONObject.parseObject(value, FileRegisterVo.class);
                        boolean validateFileRegisterVo = ResourceVoParameterValidater.INSTANCE
                                .validateFileRegisterVo(fileRegisterVo);
                        if (!validateFileRegisterVo) {
                            throw new ParameterInValidateException("资源注册时参数值不能为null!");
                        }
                        // 在这里发现了dubbox调用时的一个问题，即不能够传输null值，这可能和采取的序列化方式有关，测试时采取的是kyro
                        // 传输null值会报RemotingException: Fail to decode request
                        // due to: RpcInvocation
                        Integer updateForResourceRegister = informationResourceService.resourceRegister(
                                fileRegisterVo.getResId(), fileRegisterVo.getWhetherPermanentPersistent(),
                                fileRegisterVo.getExpiredDate(), fileRegisterVo.getIsFreeCharge());
                        resultDto = new ResultDto();
                        if (updateForResourceRegister == null || updateForResourceRegister == 0) {
                            resultDto.setResponseCode(ResourceGlobalConstants.RESOURCE_NOT_EXIST_CODE);
                            resultDto.setMessage("你需要注册的资源不存在!");
                        } else {
                            resultDto.setResponseCode(ResourceGlobalConstants.RESOURCE_OPERATE_SUCCESS);
                            resultDto.setMessage("注册资源成功!");
                        }
                    } catch (ResourceException e) {
                        LOGGER.error("注册资源失败", e);
                        resultDto = buildErrorResultDto(e.getCode(), e.getMsg());
                    } catch (Exception ex) {
                        LOGGER.error("注册资源失败", ex);
                        resultDto = buildErrorResultDto(ResourceGlobalConstants.RPC_EXCEPTION_CODE, ex.getMessage());
                    }
                } else if (ResourceCodeEnum.RESOURCE_DELETE.getCode().equals(name)) {
                    try {
                        FileDeleteVo fileDeleteVo = JSONObject.parseObject(value, FileDeleteVo.class);
                        boolean validateFileDeleteVo = ResourceVoParameterValidater.INSTANCE
                                .validateFileDeleteVo(fileDeleteVo);
                        if (!validateFileDeleteVo) {
                            throw new ParameterInValidateException("删除资源时参数不能为null!");
                        }
                        Integer deleteForResourceDelete = informationResourceService
                                .resourceDelete(fileDeleteVo.getResId());
                        resultDto = buildSuccessResultDto("删除资源成功!", null);
                        if (deleteForResourceDelete == 0 || deleteForResourceDelete == null) {
                            resultDto.setMessage("您要删除的资源不存在!");
                        }
                    } catch (ResourceException e) {
                        LOGGER.error("删除资源失败", e);
                        resultDto = buildErrorResultDto(e.getCode(), e.getMsg());
                    } catch (Exception e2) {
                        LOGGER.error("删除资源失败", e2);
                        resultDto = buildErrorResultDto(ResourceGlobalConstants.RPC_EXCEPTION_CODE,
                                "删除资源时出现服务器异常！" + e2.getMessage());
                    }
                }
            } catch (IOException e1) {
                LOGGER.error("读取失败,会输出头信息!", e1);
                resultDto = buildErrorResultDto(ResourceGlobalConstants.RPC_EXCEPTION_CODE,
                        "请求信息读取失败! " + e1.getMessage());
            }
            if (resultDto != null) {
                // 前面校验不通过的在这里直接返回
                sendResponse(ctx, resultDto);
                ctx.channel().close();
                return;
            }
        } else {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                FileUpload fileUpload = (FileUpload) data;
                if (fileUpload.isCompleted()) {
                    // 将文件持久化到磁盘
                    saveFileToDisk(fileUpload);
                }
            }
        }
    }

    /**
     * 向客户端输出响应信息
     *
     * @param channel
     */
    private void writeResponse(Channel channel) {
        LOGGER.info(responseContent.toString());
        // Convert the response content to a ChannelBuffer.
        ByteBuf buf = copiedBuffer(responseContent.toString(), CharsetUtil.UTF_8);
        LOGGER.debug(responseContent.toString());
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
     * @param ctx
     */
    private void sendResponse(ChannelHandlerContext ctx) {
        HttpResponseStatus status = null;
        String contentType = "application/json; charset=UTF-8";
        // boolean keepAlive = HttpHeaderUtil.isKeepAlive(request);
        // if (!keepAlive) {
        // ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        // } else {
        // response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        // ctx.write(response);
        // }
        if (resultDto == null) {
            status = HttpResponseStatus.BAD_REQUEST;
            resultDto = buildErrorResultDto(String.valueOf(status.code()), responseContent.toString());
        } else {
            status = HttpResponseStatus.OK;
        }
        responseContent.setLength(0);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(JSONObject.toJSONString(resultDto), CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, contentType);
        response.headers().add("Access-Control-Allow-Origin", "*");

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 发送响应信息
     *
     * @param ctx
     */
    private void sendResponse(ChannelHandlerContext ctx, ResultDto resultDto) {
        HttpResponseStatus status = HttpResponseStatus.OK;
        String contentType = "application/json; charset=UTF-8";
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(JSONObject.toJSONString(resultDto), CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, contentType);
        response.headers().add("Access-Control-Allow-Origin", "*");
        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 将文件保存到磁盘中
     *
     * @param fileUpload
     *            要上传的文件
     * @return 文件保存的信息
     */
    private String saveFileToDisk(FileUpload fileUpload) {
        StringBuilder filePath = new StringBuilder(); // full path of the new
                                                      // file to be saved
        StringBuilder fileUrl = new StringBuilder();// 文件保存的url
        String upoadedFileName = fileUpload.getFilename();
        String extension = "";
        int i = upoadedFileName.lastIndexOf('.');
        if (i > 0) {
            // get extension including the "."
            extension = upoadedFileName.substring(i);
        }
        String uniqueBaseName = getUniqueId();
        String fileName = uniqueBaseName + extension;
        filePath.append(HttpUploadServer.getPropertiesFile().getString("file_write_path", "F:\\nettyFile\\"));
        fileUrl.append(HttpUploadServer.getPropertiesFile().getString("file_base_url", "http://localhost/"));
        if (!filePath.toString().endsWith(File.separator)) {
            filePath.append(File.separator);
        }
        if (!fileUrl.toString().endsWith("/")) {
            fileUrl.append("/");
        }
        try {
            // 构造响应信息
            FileUploadDto fileUploadDto = new FileUploadDto();
            FileUploadVo fileUploadVo = (FileUploadVo) fileBaseVo;
            // 检查资源是否存在
            InformationResourceDto informationResourceDto = informationResourceService
                    .selectForResourceUpload(fileUploadVo.getMd5(), fileUploadVo.getFileSize());
            if (informationResourceDto != null) {
                fileUploadDto.setExpiredDate(informationResourceDto.getExpiredDate());
                fileUploadDto.setFilePath(informationResourceDto.getResUrl());
                fileUploadDto.setResId(informationResourceDto.getId());
            } else {
                filePath.append(ResourceTypeEnum.getFilePathByCode(fileUploadVo.getResType())).append(File.separator).append(fileUploadVo.getAppId())
                        .append(File.separator).append(fileName);
                fileUrl.append(fileUploadVo.getAppId()).append("/").append(fileName);
                File file = new File(filePath.toString());
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                }
                // 保存文件
                fileUpload.renameTo(new File(filePath.toString()));
                informationResourceDto = new InformationResourceDto();
                informationResourceDto.setIsPersistent(0);// 非持久 9表示持久
                informationResourceDto.setUploadDate(new Date());// 上传时间
                Date expireDate = DateFormatUtil.addMonth(ResourceGlobalConstants.RESERVE_TEMP_RESOURCE);
                informationResourceDto.setExpiredDate(expireDate);
                informationResourceDto.setIsFreeCharge(0);
                informationResourceDto.setDeleteStatus(0);
                informationResourceDto.setResMd5(fileUploadVo.getMd5());
                informationResourceDto.setResSize(fileUploadVo.getFileSize());
                informationResourceDto.setResType(fileUploadVo.getResType());
                informationResourceDto.setResUrl(fileUrl.toString());
                informationResourceDto.setRetainWhenExpired(
                        DateFormatUtil.addMonth(ResourceGlobalConstants.RESERVE_EXPERIED_TEMP_RESOURCE).getTime());
                Long resId = informationResourceService.resourceUpload(informationResourceDto);
                fileUploadDto.setFilePath(fileUrl.toString());
                fileUploadDto.setResId(resId);
                fileUploadDto.setExpiredDate(expireDate);
            }
            resultDto = buildSuccessResultDto("上传资源成功!", JSONObject.toJSONString(fileUploadDto));
        } catch (Exception ex) {
            LOGGER.error("数据持久化异常", ex);
            resultDto = buildErrorResultDto(ResourceGlobalConstants.RPC_EXCEPTION_CODE,
                    "上传资源保存和入db时出现异常! " + ex.getMessage());
        }
        return "success";
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(responseContent.toString(), cause);
        ctx.channel().close();
    }

    /**
     * 构造成功返回的信息
     * 
     * @param message
     * @param data
     * @return
     */
    private ResultDto buildSuccessResultDto(String message, Object data) {
        return new ResultDto(ResourceGlobalConstants.RESOURCE_OPERATE_SUCCESS, message, data);
    }

    /**
     * 构建error返回信息 如果有需要，可将异常信息放入data中返回，但要考虑到网络io的开销
     * 
     * @param errorCode
     * @param message
     * @return
     */
    private ResultDto buildErrorResultDto(String errorCode, String message) {
        return new ResultDto(errorCode, message, "");
    }

    /**
     * 生成一个唯一的id
     *
     * @return 返回id
     */
    private String getUniqueId() {
        // TODO md5较验及命名规范
        UUID uniqueId = UUID.randomUUID();
        return uniqueId.toString();
    }
}
