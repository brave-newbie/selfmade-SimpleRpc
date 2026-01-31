package com.netty.groupchat;

import java.util.Scanner;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class GroupChatClient {
    private final String host;
    private final int port;
    public GroupChatClient(String host,int port){
        this.host = host;
        this.port = port;
    }
    public void run() throws Exception{

        EventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();

        try{
            Bootstrap bootstrap = new Bootstrap().group(nioEventLoopGroup)
        .channel(NioSocketChannel.class)
        .handler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();

                pipeline.addLast("decoder",new StringDecoder());
                pipeline.addLast("encoder",new StringEncoder());
                
                pipeline.addLast(new GroupChatClientHandler());
            }
            
        });
        ChannelFuture sync = bootstrap.connect(host,port).sync();
        Channel channel = sync.channel();
        System.out.println("--------------+" + channel.localAddress() + "+-------------------");
        Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine()){
            String s = sc.nextLine();
            channel.writeAndFlush(s + "\r\n");
        }
        }finally{
            nioEventLoopGroup.shutdownGracefully();
        }
        
    }
    public static void main(String[] args) throws Exception{
        new GroupChatClient("127.0.0.1", 7000).run();
    }
}
