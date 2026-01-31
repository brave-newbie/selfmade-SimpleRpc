package com.netty.groupchat;

import java.text.SimpleDateFormat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class GroupChatServerHandler extends SimpleChannelInboundHandler<String> {

    //GlobalEventExecutor.INSTANCE是全局事件执行器，是一个单例
    private static ChannelGroup channelGroup  = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        /*
            将该客户端加入聊天的消息推送给其他在线的客户端
        */
        channelGroup.writeAndFlush("[客户端]" + channel.remoteAddress() + sdf.format(System.currentTimeMillis()) + "加入聊天 \n");
        channelGroup.add(channel);
    }

    


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress()+ " "+sdf.format(System.currentTimeMillis()) +"上线了--");
    }

    


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress()+ sdf.format(System.currentTimeMillis()) +"下线了--");
    }




    //断开连接，将断开信息推送给所有现在的客户
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();

        channelGroup.writeAndFlush("[客户端]" + channel.remoteAddress() + sdf.format(System.currentTimeMillis()) + "退出聊天 \n");
        System.out.println("当前在线人数"+channelGroup.size());
    }




    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        Channel channel = ctx.channel();

        //根据不同情况，回送不同消息
        channelGroup.forEach(ch -> {
            if(ch == channel){
                ch.writeAndFlush("[自己]"+s+"\n");
            } else {
                ch.writeAndFlush("[客户]" + channel.remoteAddress() + "说：" + s + "\n");
            }
        });
    }




    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    
}
