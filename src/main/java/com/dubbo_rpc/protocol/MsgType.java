package com.dubbo_rpc.protocol;

import lombok.Getter;

/**
 * 消息类型枚举
 */
@Getter
public enum MsgType {
    REQUEST((byte) 1),
    RESPONSE((byte) 2),
    HEARTBEAT_REQUEST((byte) 3),
    HEARTBEAT_RESPONSE((byte) 4);

    private final byte type;

    MsgType(byte type) {
        this.type = type;
    }
    
    public static MsgType findByType(byte type) {
        for (MsgType msgType : MsgType.values()) {
            if (msgType.getType() == type) {
                return msgType;
            }
        }
        return null;
    }
}
