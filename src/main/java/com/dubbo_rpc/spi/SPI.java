package com.dubbo_rpc.spi;

import java.lang.annotation.*;

/**
 * SPI 注解
 * 标记该接口是一个扩展接口
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {
}
