package com.zero.rpc.common.remoting.transport.client;

import com.zero.rpc.common.compress.CompressType;
import com.zero.rpc.common.registry.ServiceDiscovery;
import com.zero.rpc.common.remoting.constant.RpcMessageType;
import com.zero.rpc.common.remoting.dto.RpcMessage;
import com.zero.rpc.common.remoting.dto.RpcRequest;
import com.zero.rpc.common.remoting.dto.RpcResponse;
import com.zero.rpc.common.remoting.transport.codec.RpcMessageDecoder;
import com.zero.rpc.common.remoting.transport.codec.RpcMessageEncoder;
import com.zero.rpc.common.serialize.SerializationType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


/**
 * @author Zhou
 *
 * Netty 客户端
 */
@Slf4j
@Component
public class NettyClient {

    /**
     * Netty 启动器
     */
    private final Bootstrap bootstrap;
    /**
     * NIO 事件的组 id
     */
    private final NioEventLoopGroup group;
    /**
     * 管道提供器
     */
    @Autowired
    private ChannelProvider channelProvider;
    /**
     * 服务发现
     */
    @Autowired
    private ServiceDiscovery serviceDiscovery;
    /**
     * 待完成请求
     */
    @Autowired
    private UnprocessedRequests unprocessedRequests;

    public NettyClient(){

        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //设置连接超时时间为5s
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new RpcMessageDecoder());
                        pipeline.addLast(new RpcMessageEncoder());
                        pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        pipeline.addLast(new NettyRpcClientHandler());
                    }
                });
    }

    @SneakyThrows
    private Channel doConnect(InetSocketAddress inetSocketAddress){
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        // Netty的I/O操作是异步的，为connect操作设置一监听器，当操作完成后调用该监听器
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()){
                log.info("The client has connected [{}] successful!", inetSocketAddress.toString());
                //将Channel存入completableFuture中
                completableFuture.complete(channelFuture.channel());
            }else {
                throw new IllegalStateException("connect fail....");
            }
        });
        //连接操作完成后才能从completableFuture获取Channel，否则会一直阻塞在get()方法
        return completableFuture.get();
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress){
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null){
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    public void close(){
        group.shutdownGracefully();
    }

    /**
     * 消费者发送包含调用信息的rpcRequest（发送过程是异步的）
     * 返回一个CompletableFuture，后续通过该Future获取Rpc调用结果
     * 由于Netty消息发送是异步的，返回Future是非阻塞的，因此该方法是非阻塞的
     */
    public CompletableFuture<RpcResponse<Object>> sendRpcRequest(RpcRequest rpcRequest){
        //用于存放返回的结果
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        //根据rpcRequest获取调用服务名
        String rpcServiceName = rpcRequest.toRpcProperties().toRpcServiceName();
        //根据服务名获取服务提供方的地址
        InetSocketAddress serverAddress = serviceDiscovery.lookupService(rpcServiceName);
        Channel channel = getChannel(serverAddress);
        if (channel.isActive()){
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            //构建好RpcMessage
            RpcMessage rpcMsg = RpcMessage.builder()
                    .codec(SerializationType.KYRO.getCode())
                    .compress(CompressType.GZIP.getCode())
                    .messageType(RpcMessageType.REQUEST_TYPE.getCode())
                    .data(rpcRequest).build();
            //发送rpcMessage并设置监听
            channel.writeAndFlush(rpcMsg).addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()){
                    log.info("client send message: [{}]", rpcMsg);
                }else {
                    //若消息发送失败，则关闭通道，并在resultFuture中存入异常发生原因，后续在获取结果时可知发送失败
                    channelFuture.channel().close();
                    resultFuture.completeExceptionally(channelFuture.cause());
                    log.error("Send failed:", channelFuture.cause());
                }
            });
        }else {
            throw new IllegalStateException("channel is unActive!");
        }
        return resultFuture;
    }
}
