package org.ardverk.dht.storage;

import org.ardverk.dht.rsrc.Value;

public interface ValueEntity extends Value {

    public long getContentLength();
    
    public String getContentType();
}
