package com.zero.rpc.common.loadbalance.loadbalancer;

import com.zero.rpc.common.loadbalance.AbstractLoadBalance;

import java.util.List;
import java.util.Random;


/**
 * @author Zhou
 *
 * 随机算法算法实现负载均衡
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    /**
     * 使用随机算法算法实现负载均衡
     * @param serviceAddress    服务地址
     * @param rpcServiceName    RPC 服务名称
     * @return
     */
    @Override
    protected String doSelect(List<String> serviceAddress, String rpcServiceName) {
        Random random = new Random();
        return serviceAddress.get(random.nextInt(serviceAddress.size()));
    }
}
