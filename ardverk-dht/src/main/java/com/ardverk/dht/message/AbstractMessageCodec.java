package com.ardverk.dht.message;

import java.io.IOException;
import java.net.SocketAddress;

public abstract class AbstractMessageCodec implements MessageCodec {

    /**
     * 
     */
    public Message decode(SocketAddress src, byte[] data) throws IOException {
        return decode(src, data, 0, data.length);
    }
}
