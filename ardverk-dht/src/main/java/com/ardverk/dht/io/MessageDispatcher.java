package com.ardverk.dht.io;

import java.io.IOException;
import java.net.SocketAddress;

import com.ardverk.dht.message.Message;

public abstract class MessageDispatcher {

    public abstract void send(SocketAddress dst, 
            Message message) throws IOException;
    
    protected void received() {}
}
