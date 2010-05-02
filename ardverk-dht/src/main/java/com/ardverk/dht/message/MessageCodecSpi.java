package com.ardverk.dht.message;

import java.io.IOException;
import java.net.SocketAddress;

import org.ardverk.lang.NullArgumentException;

/**
 * 
 */
public abstract class MessageCodecSpi {

    private final String name;
    
    /**
     * 
     */
    protected MessageCodecSpi(String name) {
        if (name == null) {
            throw new NullArgumentException("name");
        }
        
        this.name = name;
    }
    
    /**
     * 
     */
    public String getName() {
        return name;
    }
    
    /**
     * 
     */
    public abstract byte[] encode(Message message) throws IOException;
    
    /**
     * 
     */
    public Message decode(SocketAddress src, byte[] data) throws IOException {
        return decode(src, data, 0, data.length);
    }
    
    /**
     * 
     */
    public abstract Message decode(SocketAddress src, 
            byte[] data, int offset, int length) throws IOException;
    
    @Override
    public String toString() {
        return name;
    }
}
