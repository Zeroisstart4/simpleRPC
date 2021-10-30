package com.zero.rpc.common.loadbalance.loadbalancer;

import com.zero.rpc.common.loadbalance.AbstractLoadBalance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Zhou
 *
 * 一致性 hash 算法实现负载均衡
 */
@Slf4j
@Component
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

    // 本地缓存，存放各个服务的 Hash 选择器
    private final ConcurrentHashMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();


    /**
     * 执行一致性 hash 算法实现负载均衡
     * @param serviceAddresses  服务地址
     * @param rpcServiceName    RPC 服务名称
     * @return
     * @throws UnknownHostException
     */
    @Override
    protected String doSelect(List<String> serviceAddresses, String rpcServiceName) throws UnknownHostException {
        // 获取服务地址列表的 hash 值
        int identityHashCode = System.identityHashCode(serviceAddresses);
        // 获取该服务的选择器
        ConsistentHashSelector selector = selectors.get(rpcServiceName);

        // 如果 Hash 选择器未创建或地址列表已更新，则需要重新创建 Hash 选择器
        if (selector == null || selector.identityHashCode != identityHashCode) {
            selectors.put(rpcServiceName, new ConsistentHashSelector(serviceAddresses, 160, identityHashCode));
            selector = selectors.get(rpcServiceName);
        }

        // 利用Hash选择器获取一个地址
        return selector.select();
    }

    /**
     * Hash选择器
     * 内置有存放地址节点的 Hash 环，负责通过一致性 Hash 算法获取一个服务地址
     * 这里一致性 Hash 的 Key 选取本地 IP, 即相同的 IP 总会被负载到同一台服务器上
     */
    static class ConsistentHashSelector {
        // 存放虚拟节点的哈希环
        private final TreeMap<Long, String> virtualInvokers;
        // hashCode
        private final int identityHashCode;
        // 虚拟节点数
        private final int replicaNumber;

        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode) {
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;
            this.replicaNumber = replicaNumber;

            // 向Hash环中存放虚拟节点
            for (String invoker : invokers) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    // 对 address + i 进行 md5 运算得到一个长度为 16 的字节数组
                    byte[] digest = md5(invoker + i);
                    for (int h = 0; h < 4; h++) {
                        // h = 0 时，取 digest 中下标为0~3的4个字节进行位运算
                        // h = 1 时，取 digest 中下标为4~7的4个字节进行位运算
                        // h = 2, h = 3 时过程同上
                        long m = hash(digest, h);
                        // 将 hash 到 invoker 的映射关系存储到 virtualInvokers 中，
                        // virtualInvokers 需要提供高效的查询操作，因此选用 TreeMap 作为存储结构
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        /**
         * md5 算法
         * @param key   待计算 MD5 的键
         * @return
         */
        static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

            return md.digest();
        }

        /**
         * hash 算法
         * @param digest    待 hash 数值
         * @param idx       索引下标
         * @return
         */
        static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }

        public String select() throws UnknownHostException {
            String IPAddress = InetAddress.getLocalHost().getAddress().toString();
            //将本地的IP地址进行md5和hash运算后去哈希环中寻找对应的虚拟节点
            byte[] digest = md5(IPAddress);
            //取digest数组的前四个字节进行hash运算,再将hash值传给selectForKey方法，
            return selectForKey(hash(digest, 0));
        }


        /**
         * 根据 hash 值在环上寻找所属的虚拟节点
         * @param hashCode  哈希码
         * @return
         */
        public String selectForKey(long hashCode) {

            // 寻找大于等于该 hash 值的节点
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hashCode, true).firstEntry();
            // 如果没有则循环至首节点
            if (entry == null) {
                entry = virtualInvokers.firstEntry();
            }

            return entry.getValue();
        }
    }
}
