package com.ardverk.lang;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.ardverk.io.Writable;
import org.ardverk.lang.NullArgumentException;

import com.ardverk.coding.CodingUtils;

public final class ByteArray implements Serializable, 
        Comparable<ByteArray>, Writable, Iterable<Byte> {
    
    private static final long serialVersionUID = -8073557453824859383L;

    private final byte[] data;
    
    private final int hashCode;
    
    public static ByteArray wrap(byte[] data) {
        return new ByteArray(data);
    }
    
    public static ByteArray wrap(byte[] data, int offset, int length) {
        return new ByteArray(data, offset, length);
    }
    
    public ByteArray(byte[] data) {
        this(data, 0, data.length);
    }
    
    public ByteArray(byte[] data, int offset, int length) {
        if (data == null) {
            throw new NullArgumentException("data");
        }
        
        if (offset < 0 || length < 0 || (offset+length) > data.length) {
            throw new ArrayIndexOutOfBoundsException(
                    "offset=" + offset + ", length=" + length);
        }
        
        byte[] copy = new byte[length];
        System.arraycopy(data, offset, copy, 0, length);
        
        this.data = copy;
        this.hashCode = Arrays.hashCode(copy);
    }
    
    public byte[] get() {
        return data.clone();
    }
    
    public byte get(int index) {
        if (index >= data.length) {
            throw new ArrayIndexOutOfBoundsException("index=" + index);
        }
        
        return data[index];
    }
    
    public int length() {
        return data.length;
    }

    @Override
    public int write(OutputStream out) throws IOException {
        out.write(data);
        return data.length;
    }

    @Override
    public Iterator<Byte> iterator() {
        return new Iterator<Byte>() {

            private int index = 0;
            
            @Override
            public boolean hasNext() {
                return (index < data.length);
            }

            @Override
            public Byte next() {
                if (index >= data.length) {
                    throw new NoSuchElementException();
                }
                return data[index++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ByteArray)) {
            return false;
        }
        
        return compareTo((ByteArray)o) == 0;
    }

    @Override
    public int compareTo(ByteArray o) {
        int length = length();
        if (length != o.length()) {
            return length - o.length();
        }
        
        for (int i = 0; i < length; i++) {
            int diff = (data[i] & 0xFF) - (o.data[i] & 0xFF);
            if (diff != 0) {
                return diff;
            }
        }

        return 0;
    }
    
    @Override
    public String toString() {
        return CodingUtils.encodeBase16(data);
    }
}
