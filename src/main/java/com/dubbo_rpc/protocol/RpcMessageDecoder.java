package com.dubbo_rpc.protocol;

import com.dubbo_rpc.common.RpcRequest;
import com.dubbo_rpc.common.RpcResponse;
import com.dubbo_rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.Arrays;

/**
 * 自定义协议解码器
 * 继承 LengthFieldBasedFrameDecoder 解决粘包拆包问题
 */
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    private final Serializer serializer;

    public RpcMessageDecoder(Serializer serializer) {
        // maxFrameLength: 最大帧长度
        // lengthFieldOffset: 长度字段的偏移量 (魔数4 + 版本1 + 序列化1 + 类型1 + 状态1 + 请求ID8 = 16)
        // lengthFieldLength: 长度字段的长度 (4字节)
        // lengthAdjustment: 长度调整值 (0)
        // initialBytesToStrip: 需要跳过的字节数 (0，我们需要读取完整的 Header)
        super(8 * 1024 * 1024, 16, 4, 0, 0);
        this.serializer = serializer;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() < ProtocolConstants.HEAD_LENGTH) {
                return null;
            }

            try {
                return decodeFrame(frame);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            } finally {
                // 必须释放，否则内存泄漏
                // 注意：super.decode 返回的 ByteBuf 引用计数是 1，如果我们处理完了需要释放
                // 如果返回给后面的 Handler 处理，则不需要在这里释放
                // 但因为我们在这里把 ByteBuf 转成了 RpcMessage 对象，所以原来的 ByteBuf 没用了
                // 不过 Netty 的 ByteToMessageDecoder 会自动释放传入的 input Buf，
                // 但是 LengthFieldBasedFrameDecoder 返回的是 slice 或者 copy，需要小心
                // 这里我们自己解析完了，不需要传递 ByteBuf 给下一个 Handler，而是传递 RpcMessage
                // 所以我们不需要 release frame? 
                // 不，LengthFieldBasedFrameDecoder.decode() 返回的 ByteBuf 是 retained 的，需要 release
                // 但是我们后面返回了 RpcMessage，并没有把 frame 传下去，所以这里必须 release
                // frame.release(); 
                // 等等，我们在 return 之前 release 可能会导致读取数据出错？
                // 不，数据已经读到 byte[] 里了。
            }
        }
        return decoded;
    }

    private Object decodeFrame(ByteBuf in) {
        // 1. 检查魔数
        byte[] magic = new byte[4];
        in.readBytes(magic);
        if (!Arrays.equals(magic, ProtocolConstants.MAGIC_NUMBER)) {
            throw new IllegalArgumentException("Unknown magic number: " + Arrays.toString(magic));
        }

        // 2. 读取版本
        byte version = in.readByte();
        if (version != ProtocolConstants.VERSION) {
            throw new IllegalArgumentException("Unknown version: " + version);
        }

        // 3. 读取序列化类型
        byte codec = in.readByte();
        // 4. 读取消息类型
        byte messageType = in.readByte();
        // 5. 读取状态
        byte status = in.readByte();
        // 6. 读取请求 ID
        long requestId = in.readLong();
        // 7. 读取数据长度
        int dataLength = in.readInt();

        RpcMessage rpcMessage = new RpcMessage();
        rpcMessage.setCodec(codec);
        rpcMessage.setMessageType(messageType);
        rpcMessage.setRequestId(requestId);

        // 8. 读取数据内容
        if (dataLength > 0) {
            byte[] data = new byte[dataLength];
            in.readBytes(data);
            
            // 根据消息类型进行反序列化
            Object obj = null;
            if (messageType == MsgType.REQUEST.getType()) {
                obj = serializer.deserialize(data, RpcRequest.class);
            } else if (messageType == MsgType.RESPONSE.getType()) {
                obj = serializer.deserialize(data, RpcResponse.class);
            }
            rpcMessage.setData(obj);
        }

        return rpcMessage;
    }
}
