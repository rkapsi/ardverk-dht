package com.ardverk.dht.codec;

import java.io.IOException;
import java.net.SocketAddress;

import com.ardverk.dht.message.Message;

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
