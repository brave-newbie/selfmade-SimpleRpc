package com.netty.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {
    public static void main(String[] args) throws Exception {

        //创建Netty的两个连接池
        EventLoopGroup BossGroup = null;
        EventLoopGroup WorkerGroup = null;
        try {
            BossGroup = new NioEventLoopGroup();
            WorkerGroup = new NioEventLoopGroup();

            //创建服务端启动对象
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            //设置启动对象
            serverBootstrap.group(BossGroup, WorkerGroup)  //设置两个线程池
                    .channel(NioServerSocketChannel.class)  // 使用NioServerSocketChannel作为服务器的通道实现
                    .option(ChannelOption.SO_BACKLOG, 128) // 设置线程队列得到连接个数
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 设置保持活动连接状态
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 创建一个通道初始化对象
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new NettyServerHandler());
                        }
                    });
            System.out.println("服务器 is ready...");

            //绑定端口，生成一个ChannelFuture对象
            ChannelFuture cf = serverBootstrap.bind(6668).sync();//启动服务器

            //对关闭通道进行监听
            cf.channel().closeFuture().sync();
        } finally {
            BossGroup.shutdownGracefully();
            WorkerGroup.shutdownGracefully();
        }

    }
}
