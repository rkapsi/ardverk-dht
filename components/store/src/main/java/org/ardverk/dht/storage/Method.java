package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.ardverk.dht.lang.StringValue;
import org.ardverk.io.Writable;
import org.ardverk.utils.StringUtils;

public enum Method implements Writable, StringValue {
    
    GET(HttpGet.METHOD_NAME),
    PUT(HttpPut.METHOD_NAME),
    HEAD(HttpHead.METHOD_NAME),
    DELETE(HttpDelete.METHOD_NAME);
    
    private final String value;
    
    private Method(String value) {
        this.value = value;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        StringUtils.writeString(value, out);
    }
    
    @Override
    public String stringValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    private static final Map<String, Method> VALUES 
        = new HashMap<String, Method>();
    
    static {
        for (Method method : values()) {
            Method existing = VALUES.put(method.value, method);
            if (existing != null) {
                throw new IllegalStateException(
                        "Collision: " + existing + " vs. " + method);
            }
        }
    }
    
    public static Method valueOf(InputStream in) throws IOException {
        String value = StringUtils.readString(in);
        Method method = VALUES.get(value);
        if (method != null) {
            return method;
        }
        
        throw new IllegalArgumentException(value);
    }
}
