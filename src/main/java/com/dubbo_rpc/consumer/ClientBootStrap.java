package com.dubbo_rpc.consumer;

import com.dubbo_rpc.netty.NettyClient;
import com.dubbo_rpc.publicinterface.HelloService;

public class ClientBootStrap {

    public static final String providerName = "HelloService#hello#";

    public static void main(String[] args) throws InterruptedException {
        NettyClient nettyClient = new NettyClient();

        HelloService proxy = (HelloService) nettyClient.getBean(HelloService.class, providerName);

        for (int i = 0; i < 10; i++) {
            String ans = proxy.hello("你好 dubbo " + i);
            System.out.println("第 " + (i + 1) + " 次调用结果：" + ans);
            Thread.sleep(1000);
        }
    }

}
