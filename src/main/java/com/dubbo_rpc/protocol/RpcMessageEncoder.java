package com.dubbo_rpc.protocol;

import com.dubbo_rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 自定义协议编码器
 */
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {

    private final Serializer serializer;

    public RpcMessageEncoder(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
        // 1. 写入魔数 (4B)
        out.writeBytes(ProtocolConstants.MAGIC_NUMBER);
        // 2. 写入版本号 (1B)
        out.writeByte(ProtocolConstants.VERSION);
        // 3. 写入序列化算法 (1B) - 暂时默认
        out.writeByte(msg.getCodec());
        // 4. 写入消息类型 (1B)
        out.writeByte(msg.getMessageType());
        // 5. 写入状态 (1B) - 预留，暂时填 0
        out.writeByte(0);
        // 6. 写入请求 ID (8B)
        out.writeLong(msg.getRequestId());

        // 7. 序列化消息体
        byte[] bodyBytes = null;
        int fullLength = ProtocolConstants.HEAD_LENGTH;
        
        // 如果不是心跳包，且有数据，才序列化
        if (msg.getMessageType() != MsgType.HEARTBEAT_REQUEST.getType() 
                && msg.getMessageType() != MsgType.HEARTBEAT_RESPONSE.getType()
                && msg.getData() != null) {
            bodyBytes = serializer.serialize(msg.getData());
            fullLength += bodyBytes.length;
        }

        // 8. 写入数据长度 (4B)
        if (bodyBytes != null) {
            out.writeInt(bodyBytes.length);
            // 9. 写入数据内容
            out.writeBytes(bodyBytes);
        } else {
            out.writeInt(0);
        }
    }
}
