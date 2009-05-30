package com.ardverk.dht.routing;

import java.net.SocketAddress;

import com.ardverk.dht.KUID;

public interface Contact {

    public long getCreationTime();

    public long getTimeStamp();
    
    public KUID getContactId();
    
    public SocketAddress getRemoteAddress();
}
