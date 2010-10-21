package com.ardverk.dht2;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ByteArrayValue implements Value {

    private final byte[] value;
    
    private final int offset;
    
    private final int length;
    
    public ByteArrayValue(byte[] value) {
        this(value, 0, value.length);
    }
    
    public ByteArrayValue(byte[] value, int offset, int length) {
        if (offset < 0 || length < 0 || value.length < (offset+length)) {
            throw new IllegalArgumentException(
                    "offset=" + offset + ", length=" + length);
        }
        
        this.value = value;
        
        this.offset = offset;
        this.length = length;
    }
    
    @Override
    public long getContentLength() {
        return length;
    }

    @Override
    public InputStream getContent() {
        return new ByteArrayInputStream(value, offset, length);
    }
}