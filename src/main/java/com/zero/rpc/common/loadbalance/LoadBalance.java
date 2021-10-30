package com.zero.rpc.common.loadbalance;

import java.util.List;

/**
 * @author Zhou
 *
 * 负载均衡接口
 */
public interface LoadBalance {

    /**
     * 根据负载均衡策略选择服务地址
     * @param serviceAddress    服务地址
     * @param rpcServiceName    RPC 服务名称
     * @return
     */
    String selectServiceAddress(List<String> serviceAddress, String rpcServiceName);

}
