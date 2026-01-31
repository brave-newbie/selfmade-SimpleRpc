package com.dubbo_rpc.netty;

import com.dubbo_rpc.common.RpcRequest;
import com.dubbo_rpc.common.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.Callable;

/**
 * 4. 客户端处理器（邮递员）
 * 负责把 RpcRequest 发出去，并等待接收 RpcResponse。
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter implements Callable {

    private ChannelHandlerContext context;
    private Object result;
    private RpcRequest para;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        context = ctx; // 保存连接上下文，以便发送数据
    }

    // 4.2 收到服务端寄回来的“回执单” (RpcResponse)
    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 判断消息类型是否是 RpcResponse
        if (msg instanceof RpcResponse) {
            RpcResponse response = (RpcResponse) msg;
            // 取出结果
            result = response.getResult();
        }
        // 唤醒等待的线程 (notify 对应 call 方法里的 wait)
        notify();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    // 4.1 发送工单，并阻塞等待
    @Override
    public synchronized Object call() throws Exception {
        // 封装 RpcMessage
        RpcMessage msg = new RpcMessage();
        msg.setMessageType(MsgType.REQUEST.getType());
        msg.setCodec((byte) 1); // 默认
        msg.setCompress((byte) 0);
        // 使用 UUID 的 hashcode 作为 ID，或者解析 Long
        try {
            msg.setRequestId(Long.parseLong(para.getRequestId()));
        } catch (NumberFormatException e) {
             msg.setRequestId(para.getRequestId().hashCode());
        }
        msg.setData(para);
        
        context.writeAndFlush(msg); // 发送 RpcMessage
        wait(); // 阻塞等待，直到 channelRead 收到回复并 notify
        return result; // 返回结果给代理对象
    }
    
    void setPara(RpcRequest para){
        this.para = para;
    }
}
