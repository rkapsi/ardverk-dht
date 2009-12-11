package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;

public abstract class AbstractMessage implements Message {

    private final long creationTime = System.currentTimeMillis();
    
    private final MessageId messageId;
    
    private final Contact contact;
    
    private final SocketAddress address;
    
    public AbstractMessage(MessageId messageId, 
            Contact contact, Contact destination) {
        this(messageId, contact, destination.getRemoteAddress());
    }
    
    public AbstractMessage(MessageId messageId, Contact contact, 
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
    public Contact getContact() {
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
