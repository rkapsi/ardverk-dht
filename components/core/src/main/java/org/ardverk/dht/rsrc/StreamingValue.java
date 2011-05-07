package org.ardverk.dht.rsrc;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.DHTValueFuture;
import org.ardverk.io.CloseAwareInputStream;

public class StreamingValue extends AbstractValue implements Closeable {

    private final DHTFuture<Void> future = new DHTValueFuture<Void>();
    
    private final InputStream in;
    
    private final long contentLength;
    
    public StreamingValue(InputStream in, long contentLength) {
        this.in = new CloseAwareInputStream(in) {
            @Override
            protected void complete() {
                StreamingValue.this.complete();
            }
        };
        
        this.contentLength = contentLength;
        
        if (contentLength == 0) { 
            complete();
        }
    }

    @Override
    public DHTFuture<Void> getContentFuture() {
        return future;
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
    
    private void complete() {
        future.setValue(null);
    }
}
