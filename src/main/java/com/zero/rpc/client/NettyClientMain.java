package com.zero.rpc.client;

import com.zero.rpc.ApplicationContextUtil;
import com.zero.rpc.RpcApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Zhou
 *
 * Netty 客户端启动类
 */
@SpringBootApplication
public class NettyClientMain {
    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(RpcApplication.class, args);

        HelloController helloController = ApplicationContextUtil.getBean(HelloController.class);
        helloController.test();
    }
}
