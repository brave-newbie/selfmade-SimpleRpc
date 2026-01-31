package com.dubbo_rpc.provider;

import com.dubbo_rpc.netty.NettyServer;

public class ServerBootStrap2 {
    public static void main(String[] args) {
        // 启动第二个服务实例，监听 7001 端口
        NettyServer server = new NettyServer("127.0.0.1", 7001);
        // 注册服务
        server.register(new HelloServiceImpl());
        // 启动服务
        server.start();
    }
}
