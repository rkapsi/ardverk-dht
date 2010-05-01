package com.ardverk.dht.message;

import java.io.IOException;
import java.net.SocketAddress;

import org.ardverk.lang.NullArgumentException;

public abstract class MessageCodecSpi {

    private final String name;
    
    protected MessageCodecSpi(String name) {
        if (name == null) {
            throw new NullArgumentException("name");
        }
        
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public abstract byte[] encode(Message message) throws IOException;
    
    public abstract Message decode(SocketAddress src, byte[] in) throws IOException;
    
    @Override
    public String toString() {
        return name;
    }
}
