package com.zero.rpc.common.remoting.transport.server;

import com.zero.rpc.ApplicationContextUtil;
import com.zero.rpc.common.compress.CompressType;
import com.zero.rpc.common.remoting.constant.RpcMessageType;
import com.zero.rpc.common.remoting.constant.RpcResponseCode;
import com.zero.rpc.common.remoting.dto.RpcMessage;
import com.zero.rpc.common.remoting.dto.RpcRequest;
import com.zero.rpc.common.remoting.dto.RpcResponse;
import com.zero.rpc.common.remoting.transport.handler.RpcRequestHandler;
import com.zero.rpc.common.serialize.SerializationType;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
public class NettyRpcServerHandler extends SimpleChannelInboundHandler<RpcMessage> {

    private RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler() {
        ApplicationContext applicationContext = ApplicationContextUtil.getApplicationContext();
        rpcRequestHandler = applicationContext.getBean(RpcRequestHandler.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage rpcMessage) throws Exception {

        log.info("server receive msg:[{}]", rpcMessage);
        byte messageType = rpcMessage.getMessageType();
        RpcMessage returnMsg = new RpcMessage();
        returnMsg.setCompress(CompressType.GZIP.getCode());
        returnMsg.setCodec(SerializationType.KYRO.getCode());
        if (messageType == RpcMessageType.HEARTBEAT_PING_TYPE.getCode()){
            returnMsg.setMessageType(RpcMessageType.HEARTBEAT_PONG_TYPE.getCode());
            returnMsg.setData("PONG");
        }else {
            RpcRequest rpcRequest = (RpcRequest)rpcMessage.getData();
            Object result = rpcRequestHandler.handle(rpcRequest);
            log.info(String.format("server get result: %s", result.toString()));
            returnMsg.setMessageType(RpcMessageType.RESPONSE_TYPE.getCode());
            if (ctx.channel().isActive() && ctx.channel().isWritable()){
                RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                returnMsg.setData(rpcResponse);
            }else {
                RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCode.FAIL);
                returnMsg.setData(rpcResponse);
                log.error("????????????????????????????????????");
            }
            ctx.writeAndFlush(returnMsg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        }

    }

    /**
     * ??????????????????5s????????????ping?????????????????????30s???????????????ping??????????????????????????????????????????
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE){
                log.info("30s??????????????????????????????.....");
                ctx.close();
            }else {
                super.userEventTriggered(ctx, evt);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }


}
