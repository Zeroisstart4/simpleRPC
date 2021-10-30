package com.zero.rpc.common.registry;

import com.zero.rpc.common.registry.util.CuratorUtil;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 *@author Zhou
 *
 * zookeeper 注册类
 */
@Component
public class ZkServiceRegistry implements ServiceRegistry {

    /**
     * 注册服务
     * @param rpcServiceName    RPC 服务名称
     * @param inetAddress       IP 地址
     */
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetAddress) {
        String servicePath = CuratorUtil.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName  + inetAddress.toString();
        CuratorFramework client = CuratorUtil.getClient();
        CuratorUtil.createPersistentNode(client, servicePath);
    }

}
