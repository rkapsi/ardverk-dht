package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact2;

public abstract class AbstractMessage implements Message {

    private final long creationTime = System.currentTimeMillis();
    
    private final MessageId messageId;
    
    private final Contact2 contact;
    
    private final SocketAddress address;
    
    public AbstractMessage(MessageId messageId, 
            Contact2 contact, Contact2 destination) {
        this(messageId, contact, destination.getRemoteAddress());
    }
    
    public AbstractMessage(MessageId messageId, Contact2 contact, 
            SocketAddress address) {
        
        if (messageId == null) {
            throw new NullPointerException("messageId");
        }
        
        if (contact == null) {
            throw new NullPointerException("contact");
        }
        
        if (address == null) {
            throw new NullPointerException("address");
        }
        
        this.messageId = messageId;
        this.contact = contact;
        this.address = address;
    }
    
    /**
     * 
     */
    public long getCreationTime() {
        return creationTime;
    }
    
    @Override
    public MessageId getMessageId() {
        return messageId;
    }

    @Override
    public Contact2 getContact() {
        return contact;
    }
    
    @Override
    public SocketAddress getAddress() {
        return address;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + messageId 
                    + ", " + contact + ", " + address + ")";
    }
}
