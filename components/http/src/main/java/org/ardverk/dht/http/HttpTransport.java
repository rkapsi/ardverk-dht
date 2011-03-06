package org.ardverk.dht.http;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.dht.io.transport.AbstractTransport;
import org.ardverk.dht.io.transport.TransportCallback.Outbound;
import org.ardverk.dht.message.Message;

public class HttpTransport extends AbstractTransport {

    @Override
    public SocketAddress getSocketAddress() {
        return null;
    }

    @Override
    public void send(Message message, Outbound callback, 
            long timeout, TimeUnit unit) throws IOException {
    }
}
