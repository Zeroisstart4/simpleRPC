package com.zero.rpc.common.remoting.transport.client;

import com.zero.rpc.common.remoting.dto.RpcResponse;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Zhou
 *
 * 用于存放已发送但未收到回复的请求
 */
@Component
public class UnprocessedRequests {

    /**
     * 已发送但未收到回复的请求
     */
    private final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_REQUEST_FUTURES = new ConcurrentHashMap<>();


    /**
     * 存放已发送但未收到回复的请求
     * @param requestId     请求 id
     * @param future        已发送但未收到回复的请求
     */
    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future){
        UNPROCESSED_REQUEST_FUTURES.put(requestId, future);
    }

    /**
     * 移除已收到回复的请求
     * @param rpcResponse   RPC 响应
     */
    public void complete(RpcResponse<Object> rpcResponse){
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_REQUEST_FUTURES.remove(rpcResponse.getRequestId());
        if (null != future){
            future.complete(rpcResponse);
        }else {
            throw new IllegalStateException();
        }
    }
}
