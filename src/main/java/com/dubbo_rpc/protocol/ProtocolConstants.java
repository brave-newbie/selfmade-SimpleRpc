package com.dubbo_rpc.protocol;

/**
 * 协议常量定义
 */
public class ProtocolConstants {
    // 魔数，用于校验是否是我们的协议包
    public static final byte[] MAGIC_NUMBER = {(byte) 'd', (byte) 'u', (byte) 'b', (byte) 'b'};
    public static final byte VERSION = 1;
    
    // 头部长度：魔数(4) + 版本(1) + 序列化(1) + 类型(1) + 状态(1) + 请求ID(8) + 数据长度(4) = 20
    public static final int HEAD_LENGTH = 20; 
}
