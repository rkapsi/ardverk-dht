package org.ardverk.dht.rsrc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.ardverk.io.IoUtils;

public class ValueUtils {

    public static <T extends Value> T valueOf(Class<T> clazz, Value value) throws IOException {
        InputStream in = value.getContent();
        try {
            return valueOf(clazz, in);
        } finally {
            IoUtils.close(in);
        }
    }
    
    public static <T extends Value> T valueOf(Class<T> clazz, InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[4*1024];
            int len = -1;
            while ((len = in.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        } finally {
            IoUtils.close(baos);
        }
        
        try {
            Constructor<T> constructor = clazz.getConstructor(byte[].class);
            return constructor.newInstance(baos.toByteArray());
        } catch (SecurityException e) {
            throw new IOException("SecurityException", e);
        } catch (NoSuchMethodException e) {
            throw new IOException("NoSuchMethodException", e);
        } catch (IllegalArgumentException e) {
            throw new IOException("IllegalArgumentException", e);
        } catch (InstantiationException e) {
            throw new IOException("InstantiationException", e);
        } catch (IllegalAccessException e) {
            throw new IOException("IllegalAccessException", e);
        } catch (InvocationTargetException e) {
            throw new IOException("InvocationTargetException", e);
        }
    }
    
    private ValueUtils() {}
}
