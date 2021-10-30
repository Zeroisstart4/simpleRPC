package com.zero.rpc.server;


import com.zero.rpc.ApplicationContextUtil;
import com.zero.rpc.RpcApplication;
import com.zero.rpc.common.remoting.transport.server.NettyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author zhou
 *
 * Netty 服务端启动类
 */
@SpringBootApplication
public class NettyServerMain {

    public static void main(String[] args) {

        SpringApplication.run(RpcApplication.class, args);
        NettyServer bean = ApplicationContextUtil.getBean(NettyServer.class);
        bean.start();
    }
}
