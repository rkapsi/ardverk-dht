package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;

/**
 * 
 */
public interface Message {
    
    /**
     * Returns the unique identifier of the {@link Message}.
     */
    public MessageId getMessageId();
    
    /**
     * Returns the sender's {@link Contact} information.
     */
    public Contact getContact();
    
    /**
     * Returns the receiver's {@link SocketAddress}.
     */
    public SocketAddress getAddress();
}
