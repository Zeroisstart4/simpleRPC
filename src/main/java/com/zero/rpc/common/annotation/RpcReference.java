package com.zero.rpc.common.annotation;

import java.lang.annotation.*;

/**
 * @author Zhou
 *
 * 服务调用注解 : 标注该注解的类会通过 Rpc 方式调用远程服务
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {

    String version() default "";

    String group() default "";

}
