package com.zero.rpc.server;

import com.zero.rpc.common.annotation.RpcService;
import com.zero.rpc.common.service.Hello;
import com.zero.rpc.common.service.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Zhou
 *
 * HelloService 实现类
 */
@Slf4j
@RpcService
@Component
public class HelloServiceImpl implements HelloService {

    static {
        System.out.println("HelloServiceImpl被创建");
    }

    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl返回: {}.", result);
        return result;
    }
}
