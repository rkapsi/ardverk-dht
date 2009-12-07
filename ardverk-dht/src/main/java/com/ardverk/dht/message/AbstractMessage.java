package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact;

public abstract class AbstractMessage implements Message {

    private final long creationTime = System.currentTimeMillis();
    
    private final MessageId messageId;
    
    private final Contact contact;
    
    private final Contact destination;
    
    private final long time;
    
    public AbstractMessage(MessageId messageId, Contact source, 
            Contact destination, long time) {
        
        if (messageId == null) {
            throw new NullPointerException("messageId");
        }
        
        if (source == null) {
            throw new NullPointerException("source");
        }
        
        if (destination == null) {
            throw new NullPointerException("destination");
        }
        
        this.messageId = messageId;
        this.contact = source;
        this.destination = destination;
        this.time = time;
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
    public Contact getSource() {
        return contact;
    }
    
    @Override
    public Contact getDestination() {
        return destination;
    }

    @Override
    public long getRemoteTime() {
        return time;
    }
}
