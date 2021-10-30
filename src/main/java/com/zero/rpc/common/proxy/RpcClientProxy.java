package com.zero.rpc.common.proxy;

import com.zero.rpc.common.exception.RpcErrorMessage;
import com.zero.rpc.common.exception.RpcException;
import com.zero.rpc.common.provider.RpcServiceProperties;
import com.zero.rpc.common.remoting.constant.RpcResponseCode;
import com.zero.rpc.common.remoting.dto.RpcRequest;
import com.zero.rpc.common.remoting.dto.RpcResponse;
import com.zero.rpc.common.remoting.transport.client.NettyClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 *@author Zhou
 *
 * RPC 客户端代理类
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    /**
     * Netty 客户端
     */
    private final NettyClient nettyClient;

    /**
     * RPC 服务的属性
     */
    private final RpcServiceProperties rpcServiceProperties;

    public RpcClientProxy(NettyClient nettyClient, RpcServiceProperties rpcServiceProperties) {
        this.nettyClient = nettyClient;
        if (rpcServiceProperties.getGroup() == null){
            rpcServiceProperties.setGroup("");
        }
        if (rpcServiceProperties.getVersion() == null){
            rpcServiceProperties.setVersion("");
        }
        this.rpcServiceProperties = rpcServiceProperties;
    }

    /**
     * 获取代理对象
     * @param clazz 传入服务接口class
     */
    public <T> T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     * 调用代理对象的方法实际会去调用以下逻辑
     * @param proxy     代理类
     * @param method    代理方法
     * @param args      代理参数
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 构建 RPC 请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceProperties.getGroup())
                .version(rpcServiceProperties.getVersion())
                .build();
        // 获取 RPC 响应
        RpcResponse<Object> rpcResponse = nettyClient.sendRpcRequest(rpcRequest).get();
        // 检查 RPC 请求是否合法
        check(rpcRequest, rpcResponse);
        return rpcResponse.getData();
    }

    /**
     * 检查RpcRequest是否合法
     * @param rpcRequest       RPC 请求
     * @param rpcResponse      RPC 响应
     */
    private void check(RpcRequest rpcRequest, RpcResponse<Object> rpcResponse) {
        // 服务调用失败
        if (rpcResponse == null){
            throw new RpcException(RpcErrorMessage.SERVICE_INVOCATION_FAILURE, "interfaceName:" + rpcRequest.getInterfaceName());
        }
        // 请求与响应不匹配
        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())){
            throw new RpcException(RpcErrorMessage.REQUEST_NOT_MATCH_RESPONSE, "interfaceName:" + rpcRequest.getInterfaceName());
        }
        // 服务调用失败
        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCode.SUCCESS.getCode())){
            throw new RpcException(RpcErrorMessage.SERVICE_INVOCATION_FAILURE, "interfaceName:" + rpcRequest.getInterfaceName());
        }
    }
}
