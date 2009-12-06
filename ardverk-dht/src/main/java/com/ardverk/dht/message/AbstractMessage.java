package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact;

public abstract class AbstractMessage implements Message {

    private final long creationTime = System.currentTimeMillis();
    
    private final MessageId messageId;
    
    private final Contact contact;
    
    public AbstractMessage(MessageId messageId, Contact contact) {
        
        if (messageId == null) {
            throw new NullPointerException("messageId");
        }
        
        if (contact == null) {
            throw new NullPointerException("contact");
        }
        
        this.messageId = messageId;
        this.contact = contact;
    }
    
    @Override
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
}
