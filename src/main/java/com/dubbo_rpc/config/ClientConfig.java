package com.dubbo_rpc.config;

import com.dubbo_rpc.spring.RpcClientBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 客户端配置类
 */
@Configuration
@ComponentScan(basePackages = "com.dubbo_rpc.consumer") // 扫描服务消费者
public class ClientConfig {

    @Bean
    public RpcClientBeanPostProcessor rpcClientBeanPostProcessor() {
        return new RpcClientBeanPostProcessor();
    }
}
