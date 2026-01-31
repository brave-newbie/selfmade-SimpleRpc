package com.dubbo_rpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自定义协议消息实体
 * 对应协议结构：
 * +---------------------------------------------------------------+
 * | 魔数 (4B) | 版本 (1B) | 序列化 (1B) | 类型 (1B) | 状态 (1B) |
 * +---------------------------------------------------------------+
 * |                   请求 ID (8B)                                |
 * +---------------------------------------------------------------+
 * |                   数据长度 (4B)                               |
 * +---------------------------------------------------------------+
 * |                   数据内容 (N 字节)                           |
 * +---------------------------------------------------------------+
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcMessage {
    // 消息类型：请求、响应、心跳
    private byte messageType;
    // 序列化类型
    private byte codec;
    // 压缩类型（预留）
    private byte compress;
    // 请求ID
    private long requestId;
    // 消息体数据
    private Object data;
}
