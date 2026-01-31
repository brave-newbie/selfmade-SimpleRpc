package com.dubbo_rpc.netty;

import com.dubbo_rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 通用的解码器
 * 将字节流反序列化为 Java 对象
 */
public class CommonDecoder extends ByteToMessageDecoder {
    
    private final Serializer serializer;
    private final Class<?> genericClass;

    public CommonDecoder(Serializer serializer, Class<?> genericClass) {
        this.serializer = serializer;
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 4 字节的长度头
        if (in.readableBytes() < 4) {
            return;
        }
        
        // 标记当前读取位置
        in.markReaderIndex();
        
        int dataLength = in.readInt();
        
        // 如果剩余字节不够读一个完整的包，重置读取位置，等待更多数据
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        
        Object obj = serializer.deserialize(data, genericClass);
        out.add(obj);
    }
}
