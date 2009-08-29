package com.ardverk.dht.message;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.ardverk.utils.StringUtils;

public class BencodingOutputStream extends FilterOutputStream {

    private static final Integer TRUE = Integer.valueOf(1);
    
    private static final Integer FALSE = Integer.valueOf(0);
    
    private final String encoding;
    
    public BencodingOutputStream(OutputStream out) {
        this(out, StringUtils.UTF_8);
    }
    
    public BencodingOutputStream(OutputStream out, String encoding) {
        super(out);
        
        if (encoding == null) {
            throw new NullPointerException("encoding");
        }
        
        this.encoding = encoding;
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    //@SuppressWarnings("unchecked")
    public void writeObject(Object value) throws IOException {
        
        if (value == null) {
            writeNull();
            
        } else if (value instanceof byte[]) {
            writeBytes((byte[])value);
            
        } else if (value instanceof Boolean) {
            writeBoolean((Boolean)value);
        
        } else if (value instanceof Character) {
            writeCharacter((Character)value);
            
        } else if (value instanceof Number) {
            writeNumber((Number)value);
            
        } else if (value instanceof String) {
            writeString((String)value);
            
        } else if (value instanceof Collection<?>) {
            writeCollection((Collection<?>)value);
        
        } else if (value instanceof Map<?, ?>) {
            writeMap((Map<String, ?>)value);
            
        } else if (value instanceof Enum<?>) {
            writeEnum((Enum<?>)value);
            
        } else if (value instanceof InetAddress) {
            writeInetAddress((InetAddress)value);
        
        } else if (value instanceof SocketAddress) {
            writeSocketAddress((SocketAddress)value);
        
        } else if (value.getClass().isArray()) {
            writeArray(value);
            
        } else {
            writeCustom(value);
        }
    }
    
    public void writeNull() throws IOException {
        throw new IOException("Null is not supported");
    }
    
    protected void writeCustom(Object value) throws IOException {
        throw new IOException("Cannot bencode " + value);
    }
    
    public void writeBytes(byte[] value) throws IOException {
        writeBytes(value, 0, value.length);
    }
    
    public void writeBytes(byte[] value, int offset, int length) throws IOException {
        write(Integer.toString(length).getBytes(encoding));
        write(':');
        write(value, offset, length);
    }
    
    public void writeBoolean(boolean value) throws IOException {
        if (value) {
            writeNumber(TRUE);
        } else {
            writeNumber(FALSE);
        }
    }
    
    public void writeCharacter(char value) throws IOException {
        writeString(Character.toString(value));
    }
    
    public void writeByte(byte value) throws IOException {
        writeNumber(Byte.valueOf(value));
    }
    
    public void writeShort(short value) throws IOException {
        writeNumber(Short.valueOf(value));
    }
    
    public void writeInteger(int value) throws IOException {
        writeNumber(Integer.valueOf(value));
    }
    
    public void writeLong(long value) throws IOException {
        writeNumber(Long.valueOf(value));
    }
    
    public void writeFloat(float value) throws IOException {
        writeNumber(Float.valueOf(value));
    }
    
    public void writeDouble(double value) throws IOException {
        writeNumber(Double.valueOf(value));
    }
    
    public void writeNumber(Number value) throws IOException {
        String num = value.toString();
        write('i');
        write(num.getBytes(encoding));
        write('e');
    }
    
    public void writeString(String value) throws IOException {
        writeBytes(value.getBytes(encoding));
    }
    
    public void writeCollection(Collection<?> value) throws IOException {
        write('l');
        for (Object element : value) {
            writeObject(element);
        }
        write('e');
    }
    
    public void writeMap(Map<String, ?> map) throws IOException {
        if (!(map instanceof SortedMap<?, ?>)) {
            map = new TreeMap<String, Object>(map);
        }
        
        write('d');
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            
            writeString((String)key);
            writeObject(value);
        }
        write('e');
    }
    
    public void writeEnum(Enum<?> value) throws IOException {
        writeString(value.name());
    }
    
    public void writeInetAddress(InetAddress value) throws IOException {
        writeString(value.getHostName());
    }
    
    public void writeSocketAddress(SocketAddress value) throws IOException {
        InetSocketAddress address = (InetSocketAddress)value;
        writeString(address.getHostName() + ":" + address.getPort());
    }
    
    public void writeArray(Object value) throws IOException {
        write('l');
        int length = Array.getLength(value);
        for (int i = 0; i < length; i++) {
            writeObject(Array.get(value, i));
        }
        write('e');
    }
}
