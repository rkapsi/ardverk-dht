package com.ardverk.dht.message;

import org.ardverk.lang.NullArgumentException;

import com.ardverk.dht.routing.Contact2;

public abstract class AbstractMessage implements Message {

    private final long creationTime = System.currentTimeMillis();
    
    private final MessageId messageId;
    
    private final Contact2 contact;
    
    public AbstractMessage(MessageId messageId, Contact2 contact) {
        
        if (messageId == null) {
            throw new NullArgumentException("messageId");
        }
        
        if (contact == null) {
            throw new NullArgumentException("contact");
        }
        
        this.messageId = messageId;
        this.contact = contact;
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
    public String toString() {
        return getClass().getSimpleName() 
            + "(" + messageId + ", " + contact + ")";
    }
}
