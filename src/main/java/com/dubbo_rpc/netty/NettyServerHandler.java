package com.dubbo_rpc.netty;


import com.dubbo_rpc.common.RpcRequest;
import com.dubbo_rpc.common.RpcResponse;
import com.dubbo_rpc.protocol.MsgType;
import com.dubbo_rpc.protocol.RpcMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 6. 服务端业务处理器（智能机器人）
 * 收到 RpcRequest 工单后，自动找到对应的服务类，通过反射执行方法。
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcMessage> {

    // 6.1 服务注册表：存着所有可用的服务实例
    // key = 接口全限定名 (e.g., com.dubbo_rpc.publicinterface.HelloService)
    // value = 服务实现类的实例 (e.g., new HelloServiceImpl())
    private Map<String, Object> serviceMap;

    public NettyServerHandler(Map<String, Object> serviceMap) {
        this.serviceMap = serviceMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
        byte messageType = msg.getMessageType();
        
        // 1. 处理心跳 Ping
        if (messageType == MsgType.HEARTBEAT_REQUEST.getType()) {
            System.out.println("收到客户端心跳 Ping");
            RpcMessage response = new RpcMessage();
            response.setMessageType(MsgType.HEARTBEAT_RESPONSE.getType());
            response.setCodec(msg.getCodec());
            response.setCompress(msg.getCompress());
            ctx.writeAndFlush(response);
            return;
        }

        // 2. 处理业务请求
        if (messageType == MsgType.REQUEST.getType()) {
            RpcRequest req = (RpcRequest) msg.getData();
            RpcResponse res = new RpcResponse();
            res.setRequestId(req.getRequestId());
            
            try {
                // 【查表】
                Object serviceBean = serviceMap.get(req.getClassName());
                if (serviceBean == null) {
                    throw new RuntimeException("Service not found: " + req.getClassName());
                }
                // 【反射】
                Class<?> serviceClass = serviceBean.getClass();
                Method method = serviceClass.getMethod(req.getMethodName(), req.getParameterTypes());
                Object result = method.invoke(serviceBean, req.getParameters());
                res.setResult(result);
            } catch (Exception e) {
                e.printStackTrace();
                res.setError(e);
            }
            
            // 封装响应消息
            RpcMessage responseMsg = new RpcMessage();
            responseMsg.setMessageType(MsgType.RESPONSE.getType());
            responseMsg.setCodec(msg.getCodec());
            responseMsg.setCompress(msg.getCompress());
            responseMsg.setRequestId(msg.getRequestId());
            responseMsg.setData(res);
            
            ctx.writeAndFlush(responseMsg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                System.out.println("长时间未收到客户端消息，断开连接");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
