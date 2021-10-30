package com.zero.rpc.client;

import com.zero.rpc.common.annotation.RpcReference;
import com.zero.rpc.common.service.Hello;
import com.zero.rpc.common.service.HelloService;
import org.springframework.stereotype.Component;

/**
 * @author Zhou
 *
 * Hello 控制器
 */
@Component
public class HelloController {

    /**
     * RpcReference 该注解用于服务调用，标注该注解的类会通过 Rpc 方式调用远程服务
     */
    @RpcReference
    private HelloService helloService;

    public void test() throws InterruptedException {
        String hello = helloService.hello(new Hello("111", "222xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"));

        for (int i = 0; i < 10; i++) {
            System.out.println(helloService.hello(new Hello("111", "222xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")));
        }
    }
}
