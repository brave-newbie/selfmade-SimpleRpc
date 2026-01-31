package com.dubbo_rpc.provider;

import com.dubbo_rpc.netty.NettyServer;

public class ServerBootStrap {
    public static void main(String[] args) {
        NettyServer server = new NettyServer("127.0.0.1", 7000);
        // 注册服务
        server.register(new HelloServiceImpl());
        // 启动服务
        server.start();
    }
}
