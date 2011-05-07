package org.ardverk.dht.rsrc;

import java.io.IOException;
import java.io.InputStream;

import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.NopFuture;

public class NoValue extends AbstractValue {

    public static final Value EMPTY = new NoValue();
    
    private final NopFuture<Void> future = NopFuture.withValue(null);
    
    private final InputStream in = new InputStream() {
        @Override
        public int read() throws IOException {
            return -1;
        }
    };
    
    private NoValue() {}
    
    @Override
    public DHTFuture<Void> getContentFuture() {
        return future;
    }

    @Override
    public long getContentLength() {
        return 0L;
    }

    @Override
    public InputStream getContent() {
        return in;
    }
}
