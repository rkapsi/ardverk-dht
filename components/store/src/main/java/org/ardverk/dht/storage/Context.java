package org.ardverk.dht.storage;

import org.ardverk.dht.rsrc.Key;

public class Context {

    private final Key key;
    
    private final long lastModified = System.currentTimeMillis();
    
    private final long size = 0L;
    
    private final String etag = "\"deadbeef\"";
    
    public Context(Key key) {
        this.key = key;
    }

    public Key getKey() {
        return key;
    }

    public long getLastModified() {
        return lastModified;
    }

    public long getSize() {
        return size;
    }

    public String getETag() {
        return etag;
    }
}
