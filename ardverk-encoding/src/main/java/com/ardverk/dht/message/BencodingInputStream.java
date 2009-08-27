package com.ardverk.dht.message;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ardverk.utils.StringUtils;

public class BencodingInputStream extends FilterInputStream {

    private final String encoding;
    
    public BencodingInputStream(InputStream in) {
        this(in, StringUtils.UTF_8);
    }
    
    public BencodingInputStream(InputStream in, String encoding) {
        super(in);
        
        if (encoding == null) {
            throw new NullPointerException("encoding");
        }
        
        this.encoding = encoding;
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    public Object readObject() throws IOException {
        int token = read();
        if (token == -1) {
            throw new EOFException();
        }
        
        if (token == 'e') {
            return null;
        } else if (token == 'd') {
            return readMap();
        } else if (token == 'l') {
            return readList();
        } else if (token == 'i') {
            return readNumber();
        }
        
        int length = readLength(token);
        return readBytes(length);
    }
    
    public String readString() throws IOException {
        byte[] str = (byte[])readObject();
        return new String(str, encoding);
    }
    
    public <T extends Enum<T>> T readEnum(Class<T> clazz) throws IOException {
        return Enum.valueOf(clazz, readString());
    }
    
    private byte[] readBytes(int length) throws IOException {
        byte[] bytes = new byte[length];
        int total = 0;
        while (total < bytes.length) {
            int r = read(bytes, total, bytes.length-total);
            if (r == -1) {
                throw new EOFException();
            }
            total += r;
        }
        return bytes;
    }
    
    private int readLength(int token) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append((char)token);
        
        while ((token = read()) != ':') {
            if (token == -1) {
                throw new EOFException();
            }
            
            buffer.append((char)token);
        }
        
        try {
            return Integer.parseInt(buffer.toString());
        } catch (NumberFormatException err) {
            throw new IOException("NumberFormatException", err);
        }
    }
    
    private Number readNumber() throws IOException {
        StringBuilder buffer = new StringBuilder();
        int token = -1;
        while ((token = read()) != 'e') {
            if (token == -1) {
                throw new EOFException();
            }
            
            buffer.append((char)token);
        }
        
        try {
            return Long.parseLong(buffer.toString());
        } catch (NumberFormatException err) {
            throw new IOException("NumberFormatException", err);
        }
    }
    
    private List<?> readList() throws IOException {
        List<Object> list = new ArrayList<Object>();
        Object value = null;
        while ((value = readObject()) != null) {
            list.add(value);
        }
        return list;
    }
    
    private Map<String, ?> readMap() throws IOException {
        Map<String, Object> map = new TreeMap<String, Object>();
        Object key = null;
        while ((key = readObject()) != null) {
            if (!(key instanceof byte[])) {
                throw new IOException("Key must be a byte String");
            }
            
            Object value = readObject();
            if (value == null) {
                throw new EOFException();
            }
            
            String str = new String((byte[])key, encoding);
            map.put(str, value);
        }
        return map;
    }
}
