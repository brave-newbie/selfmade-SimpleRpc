package com.dubbo_rpc.netty;

import com.dubbo_rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 通用的编码器
 * 将 Java 对象序列化为字节流
 */
public class CommonEncoder extends MessageToByteEncoder {
    
    private final Serializer serializer;
    private final Class<?> genericClass;

    public CommonEncoder(Serializer serializer, Class<?> genericClass) {
        this.serializer = serializer;
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (genericClass.isInstance(msg)) {
            byte[] data = serializer.serialize(msg);
            out.writeInt(data.length); // 写入数据长度
            out.writeBytes(data);      // 写入数据内容
        }
    }
}
