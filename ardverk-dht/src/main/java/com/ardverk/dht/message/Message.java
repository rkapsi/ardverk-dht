package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;

/**
 * 
 */
public interface Message {
    
    /**
     * Returns the unique identifier of the {@link Message}
     */
    public MessageId getMessageId();
    
    /**
     * Returns the origin of the {@link Message}
     */
    public Contact getContact();
    
    /**
     * Returns the destination {@link SocketAddress} of the {@link Message}.
     */
    public SocketAddress getAddress();
}
