package com.zero.rpc.common.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.zero.rpc.common.remoting.dto.RpcRequest;
import com.zero.rpc.common.remoting.dto.RpcResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Zhou
 *
 * Kyro 序列化
 */
public class KyroSerializer implements Serializer {

    /**
     * Kryo 是非线程安全的，使用 ThreadLocal 为每个线程配置一个 Kryo，互不干扰,保证线程安全
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(()->{
        Kryo kryo = new Kryo();
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);
        return kryo;
    });

    /**
     * 序列化
     * @param object
     * @return
     */
    @Override
    public byte[] serialize(Object object) {
        try(ByteArrayOutputStream out = new ByteArrayOutputStream();
            Output output = new Output(out)) {
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output, object);
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (IOException e) {
            throw new RuntimeException("Serialization failed");
        }
    }

    /**
     * 反序列化
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try(ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            Input input = new Input(in)) {
            Kryo kryo = kryoThreadLocal.get();
            T object = kryo.readObject(input, clazz);
            return object;
        }catch (IOException e) {
            throw new RuntimeException("Deserialization failed");
        }
    }
}
