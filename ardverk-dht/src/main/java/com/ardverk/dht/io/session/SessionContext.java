package com.ardverk.dht.io.session;

import java.net.SocketAddress;

public interface SessionContext {

    public SocketAddress getLocalAddress();
    
    public SocketAddress getRemoteAddress();
}
