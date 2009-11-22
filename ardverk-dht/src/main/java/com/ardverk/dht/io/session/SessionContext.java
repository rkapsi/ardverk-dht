package com.ardverk.dht.io.session;

import java.net.InetSocketAddress;

public interface SessionContext {

    public InetSocketAddress getLocalAddress();
    
    public InetSocketAddress getRemoteAddress();
}
