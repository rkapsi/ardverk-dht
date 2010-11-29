package com.ardverk.dht.codec;

import java.io.IOException;
import java.net.SocketAddress;

import com.ardverk.dht.message.Message;

/**
 * A {@link MessageCodec} encodes and decodes {@link Message}s.
 */
public interface MessageCodec {
    
    /**
     * Encodes the given {@link Message}.
     */
    public abstract byte[] encode(Message message) throws IOException;
    
    /**
     * Decodes the given bytes and returns it as a {@link Message}.
     */
    public Message decode(SocketAddress src, byte[] data) throws IOException;
    
    /**
     * Decodes the given bytes and returns it as a {@link Message}.
     */
    public abstract Message decode(SocketAddress src, 
            byte[] data, int offset, int length) throws IOException;
}
