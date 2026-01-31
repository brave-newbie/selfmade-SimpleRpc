package com.dubbo_rpc.consumer;

import com.dubbo_rpc.config.ClientConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringClientBootStrap {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ClientConfig.class);
        HelloController helloController = context.getBean(HelloController.class);
        helloController.test();
    }
}
