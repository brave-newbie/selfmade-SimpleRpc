package com.netty.simple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/*
1.自定义一个Handler需要继承Netty规定好的某个HandlerContext（规范）
*/
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
//读取实际数据（客户端发来的数据）
/*
1.ChannelHandlerContext：上下文对象，含有pipeline，channel，地址
2.Object msg：就是客户端发送的数据，默认Object
 */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("server ctx =" + ctx);
        //将msg转成ByteBuf
        //这里的ByteBuf是Netty提供的，不是NIO的ByteBuffer
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("客户端发送的消息是：" + buf.toString(CharsetUtil.UTF_8));
        System.out.println("客户端地址" + ctx.channel().remoteAddress());
    }


    //读取数据完毕
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello,客户端~(>^ω^<)喵",CharsetUtil.UTF_8));
    }
    //异常处理

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }
}
