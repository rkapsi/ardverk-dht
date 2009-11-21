package com.ardverk.dht.io;

import java.io.IOException;
import java.net.SocketAddress;

import com.ardverk.dht.message.Message;

public interface MessageDispatcher {

    public void send(SocketAddress dst, Message message) throws IOException;
}
