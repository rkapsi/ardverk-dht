package org.ardverk.dht.message;

import java.io.IOException;
import java.io.InputStream;

import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.NopFuture;

/**
 * 
 */
public interface Content {
    
    public static final NopFuture<Void> FUTURE 
        = new NopFuture<Void>((Void)null);
    
    /**
     * 
     */
    public DHTFuture<Void> getContentFuture();
    
    /**
     * 
     */
    public long getContentLength();
    
    /**
     * 
     */
    public InputStream getContent() throws IOException;
    
    /**
     * 
     */
    public byte[] getContentAsBytes() throws IOException;
}