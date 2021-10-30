package com.zero.rpc.common.loadbalance.loadbalancer;

import com.zero.rpc.common.loadbalance.AbstractLoadBalance;

import java.util.List;

/**
 * @author Zhou
 *
 * 循环算法实现负载均衡
 */
public class RoundRobinBalance extends AbstractLoadBalance {

    /**
     * 当前服务地址索引
     */
    private static int index = 0;

    /**
     * 同步方法保证线程安全，同时使用循环算法实现负载均衡
     * @param serviceAddress    服务地址
     * @param rpcServiceName    RPC 服务名称
     * @return
     */
    @Override
    protected synchronized String doSelect(List<String> serviceAddress, String rpcServiceName) {
        if (index >= serviceAddress.size()){
            index = 0;
        }
        return serviceAddress.get(index++);
    }
}
