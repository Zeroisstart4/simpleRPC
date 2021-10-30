package com.zero.rpc.common.provider;

import com.zero.rpc.common.exception.RpcErrorMessage;
import com.zero.rpc.common.exception.RpcException;
import com.zero.rpc.common.registry.ServiceRegistry;
import com.zero.rpc.common.remoting.transport.server.NettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *@author Zhou
 *
 * RPC 服务接口 ServiceProvider 实现类
 */
@Slf4j
@Component
public class ServiceProviderImpl implements ServiceProvider {

    /**
     * 服务的实现类存储在服务提供方的一个Map中
     * 服务消费者通过远程请求在Map中查询得到服务的实现类并返回
     * 注意：每个服务器只会在Map中存放属于自己的实现类（不会存放属于其他Server的实现类）！！
     *      服务消费者在请求服务时也只会路由到某个实现服务的Server上！！
     *      因此Map中只存放属于自己的实现类是合理的！！
     */


    /**
     * 用于记录 RPC 服务， 键为 RPC 服务名称， 值为 RPC 服务实体
     */
    private final Map<String, Object> serviceMap;
    /**
     * 用于记录已注册的 RPC 服务，使用 Set 避免重复
     */
    private final Set<String> registeredService;
    /**
     * RPC 服务注册类
     */
    @Autowired
    private ServiceRegistry serviceRegistry;


    public ServiceProviderImpl() {
        // 使用 ConcurrentHashMap 与 ConcurrentHashMap.newKeySet() 保证线程安全
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void publishService(Object service) {
        this.publishService(service, RpcServiceProperties.builder().group("").version("").build());
    }

    /**
     * 发布服务
     * @param service                   RPC 服务实体
     * @param rpcServiceProperties      RPC 服务属性
     */
    @Override
    public void publishService(Object service, RpcServiceProperties rpcServiceProperties) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            // 该实现类注册的服务接口
            Class<?> serviceRaletedInterface = service.getClass().getInterfaces()[0];
            // 获取服务接口名称
            String serviceName = serviceRaletedInterface.getCanonicalName();
            rpcServiceProperties.setServiceName(serviceName);
            this.addService(service, rpcServiceProperties);
            serviceRegistry.registerService(rpcServiceProperties.getServiceName(), new InetSocketAddress(host, NettyServer.PORT));
            log.info("服务已成功注册:" + rpcServiceProperties.toRpcServiceName());
        }catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }

    /**
     * 获取 RPC 服务
     * @param rpcServiceProperties  RPC 服务属性
     * @return
     */
    @Override
    public Object getService(RpcServiceProperties rpcServiceProperties) {
        Object service = serviceMap.get(rpcServiceProperties.toRpcServiceName());
        if (null == service) {
            throw new RpcException(RpcErrorMessage.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    /**
     * 添加 RPC 服务
     * @param service               RPC 服务实体
     * @param rpcServiceProperties  RPC 服务属性
     */
    private void addService(Object service, RpcServiceProperties rpcServiceProperties) {
        String rpcServiceName = rpcServiceProperties.getServiceName();
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, service);
        log.info("Add service: {} and interfaces:{}", rpcServiceName, service.getClass().getInterfaces()[0]);
    }
}
