package org.ardverk.dht.rsrc;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class StreamingValue extends AbstractValue implements Closeable {
    
    private final InputStream in;
    
    private final long contentLength;
    
    public StreamingValue(InputStream in, long contentLength) {
        this.in = in;
        this.contentLength = contentLength;
    }
    
    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public InputStream getContent() {
        return in;
    }
    
    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public boolean isStreaming() {
        return true;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
