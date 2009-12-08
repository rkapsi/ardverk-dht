package com.ardverk.dht.message;

import java.io.IOException;

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
    
    public abstract byte[] encode(Message message) throws IOException;
    
    public abstract Message decode(byte[] in) throws IOException;
    
    @Override
    public String toString() {
        return name;
    }
}
