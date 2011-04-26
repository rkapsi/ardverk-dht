package org.ardverk.dht.storage;

import java.io.InputStream;


public interface Resource {
    
    public long getContentLength();
    
    public InputStream getContent();
    
    public byte[] getContentAsBytes();
}
