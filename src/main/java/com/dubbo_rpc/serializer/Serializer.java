package com.dubbo_rpc.serializer;

import com.dubbo_rpc.spi.SPI;

/**
 * 序列化接口
 * 策略模式：可以有 JSON、Protobuf、Hessian 等多种实现
 */
@SPI
public interface Serializer {
    
    /**
     * 序列化
     * @param object 要序列化的对象
     * @return 字节数组
     */
    byte[] serialize(Object object);

    /**
     * 反序列化
     * @param bytes 字节数组
     * @param clazz 目标类
     * @param <T> 泛型
     * @return 反序列化后的对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
