package com.tbkk.yoyo.rpc.net.serializer.impl;

import com.tbkk.yoyo.rpc.common.exception.YoyoRpcException;
import com.tbkk.yoyo.rpc.net.serializer.AbstractSerializer;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protostuff util
 * xuxueli 2015-10-29 18:53:43
 */
public class ProtostuffSerializer extends AbstractSerializer {

    private static Objenesis objenesis = new ObjenesisStd(true);
    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<Class<?>, Schema<?>>();

    private static <T> Schema<T> getSchema(Class<T> cls) {
        @SuppressWarnings("unchecked")
		Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            if (schema != null) {
                cachedSchema.put(cls, schema);
            }
        }
        return schema;
    }
    
    @Override
	public <T> byte[] serialize(T obj) {
    	@SuppressWarnings("unchecked")
		Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new YoyoRpcException(e);
        } finally {
            buffer.clear();
        }
	}

	@Override
	public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
		try {
            T message = (T) objenesis.newInstance(clazz);
            Schema<T> schema = getSchema(clazz);
            ProtostuffIOUtil.mergeFrom(bytes, message, schema);
            return message;
        } catch (Exception e) {
            throw new YoyoRpcException(e);
        }
	}
    
}
