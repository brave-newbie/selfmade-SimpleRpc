package com.dubbo_rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务提供者注解
 * 标记在实现类上，表示该类是一个 RPC 服务
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component // 继承 Component，使其能被 Spring 扫描
public @interface RpcService {
    // 服务接口类
    Class<?> interfaceClass() default void.class;
    // 服务版本
    String version() default "";
}
