package org.ardverk.dht.storage.message;

import org.ardverk.dht.rsrc.Value;

public interface ValueEntity extends Value {

    public long getContentLength();
    
    public String getContentType();
}
