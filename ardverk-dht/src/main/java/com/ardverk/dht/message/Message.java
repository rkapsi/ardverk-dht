package com.ardverk.dht.message;

import java.net.InetAddress;

import com.ardverk.dht.routing.Contact;

public interface Message {

    public long getCreationTime();
    
    public MessageId getMessageId();
    
    public Contact getContact();
    
    public long getRemoteTime();
    
    public InetAddress getAddress();
}
