package com.ardverk.dht.message;

import java.io.IOException;

import com.ardverk.dht.io.SessionContext;

public abstract class MessageCodecSpi {

    private final String name;
    
    protected MessageCodecSpi(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public abstract byte[] encode(SessionContext context, Message message) throws IOException;
    
    public abstract Message decode(SessionContext context, byte[] in) throws IOException;
    
    @Override
    public String toString() {
        return name;
    }
}
