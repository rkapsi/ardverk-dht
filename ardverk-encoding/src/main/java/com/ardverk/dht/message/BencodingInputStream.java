package com.ardverk.dht.message;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
        
        return readObject0(token);
    }
    
    private Object readObject0(int token) throws IOException {
        if (token == 'd') {
            return readMap0();
        } else if (token == 'l') {
            return readList0();
        } else if (token == 'i') {
            return readNumber0();
        }
        
        return readBytes0(token);
    }
    
    public byte[] readBytes() throws IOException {
        int token = read();
        if (token == -1) {
            throw new EOFException();
        }
        
        return readBytes0(token);
    }
    
    private byte[] readBytes0(int token) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append((char)token);
        
        while ((token = read()) != ':') {
            if (token == -1) {
                throw new EOFException();
            }
            
            buffer.append((char)token);
        }
        
        int length = Integer.parseInt(buffer.toString());
        byte[] data = new byte[length];
        
        int total = 0;
        while (total < data.length) {
            int r = read(data, total, data.length-total);
            if (r == -1) {
                throw new EOFException();
            }
            total += r;
        }
        
        return data;
    }
    
    public String readString() throws IOException {
        return new String(readBytes(), encoding);
    }
    
    public <T extends Enum<T>> T readEnum(Class<T> clazz) throws IOException {
        return Enum.valueOf(clazz, readString());
    }
    
    public char readCharacter() throws IOException {
        return readString().charAt(0);
    }
    
    public boolean readBoolean() throws IOException {
        return readInteger() != 0;
    }
    
    public byte readByte() throws IOException {
        return readNumber().byteValue();
    }
    
    public short readShort() throws IOException {
        return readNumber().shortValue();
    }
    
    public int readInteger() throws IOException {
        return readNumber().intValue();
    }
    
    public float readFloat() throws IOException {
        return readNumber().floatValue();
    }
    
    public long readLong() throws IOException {
        return readNumber().longValue();
    }
    
    public double readDouble() throws IOException {
        return readNumber().doubleValue();
    }
    
    public Number readNumber() throws IOException {
        int token = read();
        if (token == -1) {
            throw new EOFException();
        }
        
        if (token != 'i') {
            throw new IOException();
        }
        
        return readNumber0();
    }
    
    private Number readNumber0() throws IOException {
        StringBuilder buffer = new StringBuilder();
        
        boolean decimal = false;
        int token = -1;
        
        while ((token = read()) != 'e') {
            if (token == -1) {
                throw new EOFException();
            }
            
            if (token == '.') {
                decimal = true;
            }
            
            buffer.append((char)token);
        }
        
        try {
            if (decimal) {
                return new BigDecimal(buffer.toString());
            } else {
                return new BigInteger(buffer.toString());
            }
        } catch (NumberFormatException err) {
            throw new IOException("NumberFormatException", err);
        }
    }
    
    public List<?> readList() throws IOException {
        int token = read();
        if (token == -1) {
            throw new EOFException();
        }
        
        if (token != 'l') {
            throw new IOException();
        }
        
        return readList0();
    }
    
    private List<?> readList0() throws IOException {
        List<Object> list = new ArrayList<Object>();
        int token = -1;
        while ((token = read()) != 'e') {
            if (token == -1) {
                throw new EOFException();
            }
            
            list.add(readObject0(token));
        }
        return list;
    }
    
    public Map<String, ?> readMap() throws IOException {
        int token = read();
        if (token == -1) {
            throw new EOFException();
        }
        
        if (token != 'd') {
            throw new IOException();
        }
        
        return readMap0();
    }
    
    private Map<String, ?> readMap0() throws IOException {
        Map<String, Object> map = new TreeMap<String, Object>();
        int token = -1;
        while ((token = read()) != 'e') {
            if (token == -1) {
                throw new EOFException();
            }
            
            String key = new String(readBytes0(token), encoding);
            Object value = readObject();
            
            map.put(key, value);
        }
        
        return map;
    }
    
    public Object[] readArray() throws IOException {
        return readList().toArray(new Object[0]);
    }
    
    public InetAddress readInetAddress() throws IOException {
        return InetAddress.getByName(readString());
    }
    
    public SocketAddress readSocketAddress() throws IOException {
        String value = readString();
        int p = value.indexOf(":");
        if (p == -1) {
            throw new IOException("value=" + value);
        }
        
        return new InetSocketAddress(value.substring(0, p), 
                Integer.parseInt(value.substring(++p)));
    }
}
