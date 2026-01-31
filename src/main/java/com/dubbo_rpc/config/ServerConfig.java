package com.dubbo_rpc.config;

import com.dubbo_rpc.netty.NettyServer;
import com.dubbo_rpc.spring.RpcServerBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 服务端配置类
 */
@Configuration
@ComponentScan(basePackages = "com.dubbo_rpc.provider") // 扫描服务提供者
public class ServerConfig {

    @Bean
    public NettyServer nettyServer() {
        return new NettyServer("127.0.0.1", 7000);
    }

    @Bean
    public RpcServerBeanPostProcessor rpcServerBeanPostProcessor(NettyServer nettyServer) {
        return new RpcServerBeanPostProcessor(nettyServer);
    }
}
