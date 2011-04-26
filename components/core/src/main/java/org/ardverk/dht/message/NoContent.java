package org.ardverk.dht.message;

import java.io.InputStream;

import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.io.NopInputStream;
import org.ardverk.lang.Bytes;

public class NoContent implements Content {

    public static final NoContent EMPTY = new NoContent();
    
    private NoContent() {}
    
    @Override
    public DHTFuture<Void> getContentFuture() {
        return FUTURE;
    }

    @Override
    public long getContentLength() {
        return 0L;
    }

    @Override
    public InputStream getContent() {
        return new NopInputStream();
    }

    @Override
    public byte[] getContentAsBytes() {
        return Bytes.EMPTY;
    }
}