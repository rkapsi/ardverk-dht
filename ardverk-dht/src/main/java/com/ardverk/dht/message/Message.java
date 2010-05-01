package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact2;

/**
 * 
 */
public interface Message {
    
    /**
     * Returns the unique identifier of the {@link Message}.
     */
    public MessageId getMessageId();
    
    /**
     * Returns the sender's {@link Contact2} information.
     */
    public Contact2 getContact();
    
    /**
     * Returns the receiver's {@link SocketAddress}.
     */
    public SocketAddress getAddress();
}
