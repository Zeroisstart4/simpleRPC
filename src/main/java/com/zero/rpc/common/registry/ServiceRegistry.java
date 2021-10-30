package com.zero.rpc.common.registry;

import java.net.InetSocketAddress;

/**
 * @author Zhou
 *
 * zookeeper 服务注册接口
 */
public interface ServiceRegistry {

    /**
     * 注册服务
     * @param rpcServiceName    RPC 服务名称
     * @param inetAddress       IP 地址
     */
    void registerService(String rpcServiceName, InetSocketAddress inetAddress);

}
