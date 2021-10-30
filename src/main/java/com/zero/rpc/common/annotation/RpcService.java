package com.zero.rpc.common.annotation;

import java.lang.annotation.*;

/**
 * @author Zhou
 *
 * 服务注册注解: 标注在服务的实现类上
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface RpcService {

    String version() default "";

    String group() default "";

}
