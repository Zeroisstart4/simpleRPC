package com.zero.rpc.common.remoting.transport;

import com.zero.rpc.common.remoting.dto.RpcRequest;

//该接口用于发送Rpc请求并获取结果
public interface RpcRequestTransport {

    Object sendRpcRequest(RpcRequest rpcRequest);

}
