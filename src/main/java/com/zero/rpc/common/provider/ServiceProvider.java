package com.zero.rpc.common.provider;

/**
 *@author Zhou
 *
 * RPC 服务接口
 */
public interface ServiceProvider {

    /**
     * 发布 RPC 服务
     * @param service
     */
    void publishService(Object service);

    void publishService(Object service, RpcServiceProperties rpcServiceProperties);

    /**
     * 获取 RPC 服务
     * @param rpcServiceProperties
     * @return
     */
    Object getService(RpcServiceProperties rpcServiceProperties);

}
