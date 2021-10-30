package com.zero.rpc.common.registry;

import java.net.InetSocketAddress;

/**
 * @author Zhou
 *
 * zookeeper 服务发现接口
 */
public interface ServiceDiscovery {

    /**
     * 返回指定服务的地址
     * @param rpcServiceName
     * @return
     */
    InetSocketAddress lookupService(String rpcServiceName);
}
