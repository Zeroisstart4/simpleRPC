package com.zero.rpc.common.loadbalance;

import java.net.UnknownHostException;
import java.util.List;

/**
 * @author Zhou
 *
 * 模板模式使用， 抽象负载均衡类
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    /**
     * 根据负载均衡策略选择服务地址
     * @param serviceAddress    服务地址
     * @param rpcServiceName    RPC 服务名称
     * @return
     */
    @Override
    public String selectServiceAddress(List<String> serviceAddress, String rpcServiceName) {
        if (serviceAddress == null || serviceAddress.size() == 0){
            return null;
        }
        if (serviceAddress.size() == 1){
            return serviceAddress.get(0);
        }
        try {
            return doSelect(serviceAddress, rpcServiceName);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 执行负载均衡策略并返回选择的服务地址
     * @param serviceAddress    服务地址
     * @param rpcServiceName    RPC 服务名称
     * @return
     * @throws UnknownHostException
     */
    protected abstract String doSelect(List<String> serviceAddress, String rpcServiceName) throws UnknownHostException;
}
