package com.zero.rpc.common.remoting.transport.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Zhou
 *
 * 管道供给类， 用于管理连接
 */
@Slf4j
@Component
public class ChannelProvider {

    /**
     * key 为服务提供方的地址
     */
    private Map<String, Channel> channelMap;

    public ChannelProvider() {
        channelMap = new ConcurrentHashMap<>();
    }

    /**
     * 获取指定 IP 对应的管道
     * @param inetSocketAddress     IP 地址
     * @return
     */
    public Channel get(InetSocketAddress inetSocketAddress){

        String key = inetSocketAddress.toString();
        if (channelMap.containsKey(key)){
            Channel channel = channelMap.get(key);
            //判断该连接是可用的，若不可用则从 map 中移除
            if (channel != null && channel.isActive()){
                return channel;
            }else {
                channelMap.remove(channel);
            }
        }
        return null;
    }

    /**
     * 添加 IP 地址与管道的映射
     * @param inetSocketAddress     IP 地址
     * @param channel               管道
     */
    public void set(InetSocketAddress inetSocketAddress, Channel channel){
        String key = inetSocketAddress.toString();
        channelMap.put(key, channel);
    }

    /**
     * 移除 IP 地址映射
     * @param inetSocketAddress     IP 地址
     */
    public void remove(InetSocketAddress inetSocketAddress){
        String key = inetSocketAddress.toString();
        if (channelMap.containsKey(key)){
            channelMap.remove(key);
        }
    }

}
