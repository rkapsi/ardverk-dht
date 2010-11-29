package com.ardverk.dht.codec;

import java.io.IOException;
import java.net.SocketAddress;

import com.ardverk.dht.message.Message;

/**
 * An abstract implementation of {@link MessageCodec}
 */
public abstract class AbstractMessageCodec implements MessageCodec {
    
    @Override
    public Message decode(SocketAddress src, byte[] data) throws IOException {
        return decode(src, data, 0, data.length);
    }
}
