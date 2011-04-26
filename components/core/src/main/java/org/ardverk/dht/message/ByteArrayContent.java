package org.ardverk.dht.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.utils.StringUtils;

public class ByteArrayContent implements Content {
    
    private final byte[] content;
    
    private final int offset;
    
    private final int length;
    
    public ByteArrayContent(byte[] content) {
        this(content, 0, content.length);
    }
    
    public ByteArrayContent(byte[] content, int offset, int length) {
        this.content = content;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public DHTFuture<Void> getContentFuture() {
        return FUTURE;
    }

    @Override
    public long getContentLength() {
        return length;
    }

    @Override
    public InputStream getContent() {
        return new ByteArrayInputStream(content, offset, length);
    }

    @Override
    public byte[] getContentAsBytes() {
        if (offset == 0 && length == content.length) {
            return content;
        }
        
        byte[] copy = new byte[length];
        System.arraycopy(content, offset, copy, 0, length);
        return copy;
    }
    
    @Override
    public String toString() {
        return StringUtils.toString(getContentAsBytes());
    }
    
    public static ByteArrayContent valueOf(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4 * 1024];
        int len = -1;
        while ((len = in.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return new ByteArrayContent(baos.toByteArray());
    }
}
