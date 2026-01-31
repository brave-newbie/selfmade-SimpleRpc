package com.dubbo_rpc.provider;

import com.dubbo_rpc.config.ServerConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringServerBootStrap {
    public static void main(String[] args) {
        new AnnotationConfigApplicationContext(ServerConfig.class);
    }
}
