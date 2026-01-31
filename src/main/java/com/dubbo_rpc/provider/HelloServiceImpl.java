package com.dubbo_rpc.provider;

import com.dubbo_rpc.annotation.RpcService;
import com.dubbo_rpc.publicinterface.HelloService;

@RpcService(interfaceClass = HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String msg) {
        System.out.println("收到消息: " + msg);
        return "你好, 我已收到你的消息: " + msg;
    }
}
