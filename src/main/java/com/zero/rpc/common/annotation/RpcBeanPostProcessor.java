package com.zero.rpc.common.annotation;

import com.zero.rpc.common.provider.RpcServiceProperties;
import com.zero.rpc.common.provider.ServiceProvider;
import com.zero.rpc.common.proxy.RpcClientProxy;
import com.zero.rpc.common.remoting.transport.client.NettyClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;


/**
 * @author Zhou
 *
 * RPC 的 Bean 后处理器,用于检测 RpcService、RpcReference 注解
 */
@Component
@Slf4j
public class RpcBeanPostProcessor implements BeanPostProcessor {

    /**
     * RPC 服务
     */
    @Autowired
    private ServiceProvider serviceProvider;

    /**
     * Netty 客户端
     */
    @Autowired
    private NettyClient nettyClient;

    /**
     * 初始化前的后处理，若发现该 Bean 被 @RpcService 标注则注册服务
     * @param bean          待初始化的 Bean 对象
     * @param beanName      Bean 对象名称
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 检测 Bean 对象是否含有 @RpcService 注解，若有，则将注解包含信息加入 RpcServiceProperties，并注册 RPC 服务
        if (bean.getClass().isAnnotationPresent(RpcService.class)){
            RpcService annotation = bean.getClass().getAnnotation(RpcService.class);
            RpcServiceProperties properties = RpcServiceProperties.builder()
                    .group(annotation.group())
                    .version(annotation.version())
                    .build();
            // 注册服务
            serviceProvider.publishService(bean, properties);
        }
        return bean;
    }

    /**
     * 若发现某属性被 @RpcReference 标注则将为该属性赋值为动态代理对象
     * @param bean          待初始化的 Bean 对象
     * @param beanName      Bean 对象名称
     * @return
     * @throws BeansException
     */
    @SneakyThrows
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 获取成员变量
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        // 检查成员变量是否有 RpcReference 注解，若有，则进行动态代理
        for (Field field: declaredFields){
            RpcReference annotation = field.getAnnotation(RpcReference.class);
            if (annotation != null){
                RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                        .version(annotation.version())
                        .group(annotation.group()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(nettyClient, rpcServiceProperties);
                Object proxy = rpcClientProxy.getProxy(field.getType());
                field.setAccessible(true);
                field.set(bean, proxy);
            }
        }
        return bean;
    }
}
