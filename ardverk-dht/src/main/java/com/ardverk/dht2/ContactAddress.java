package com.ardverk.dht2;

import java.net.SocketAddress;

import com.ardverk.dht.KUID;

public interface ContactAddress {
    
    public KUID getContactId();
    
    public SocketAddress getContactAddress();
}