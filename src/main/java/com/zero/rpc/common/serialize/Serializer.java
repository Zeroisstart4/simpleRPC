package com.zero.rpc.common.serialize;

/**
 * @author Zhou
 *
 * 序列化与反序列化接口
 */
public interface Serializer {

    /**
     * 序列化
     * @param object
     * @return
     */
    byte[] serialize(Object object);

    /**
     * 反序列化
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);

}
