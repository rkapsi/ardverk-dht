package com.ardverk.dht.message;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * 
 */
public interface MessageCodec {
    
    /**
     * 
     */
    public abstract byte[] encode(Message message) throws IOException;
    
    /**
     * 
     */
    public Message decode(SocketAddress src, byte[] data) throws IOException;
    
    /**
     * 
     */
    public abstract Message decode(SocketAddress src, 
            byte[] data, int offset, int length) throws IOException;
}
