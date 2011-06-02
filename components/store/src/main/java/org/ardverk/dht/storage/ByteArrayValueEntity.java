package org.ardverk.dht.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.protocol.HTTP;
import org.ardverk.utils.StringUtils;

public class ByteArrayValueEntity extends AbstractValueEntity {

    private final byte[] data;
    
    private final int offset;
    
    private final int length;
    
    public ByteArrayValueEntity(byte[] data) {
        this(data, 0, data.length);
    }
    
    public ByteArrayValueEntity(byte[] data, int offset, int length) {
        super(HTTP.OCTET_STREAM_TYPE, length);
        
        this.data = data;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public boolean isStreaming() {
        return false;
    }

    @Override
    public InputStream getContent() throws IOException {
        return new ByteArrayInputStream(data);
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(data, offset, length);
    }
    
    @Override
    public String toString() {
        return StringUtils.toString(data, offset, length);
    }
}
