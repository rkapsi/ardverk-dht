package com.ardverk.dht.message;

import java.net.InetAddress;

import com.ardverk.dht.routing.Contact;

public abstract class AbstractMessage implements Message {

    private final long creationTime = System.currentTimeMillis();
    
    private final MessageId messageId;
    
    private final Contact contact;
    
    private final long time;
    
    private final InetAddress address;
    
    public AbstractMessage(MessageId messageId, Contact contact, 
            long time, InetAddress address) {
        
        if (messageId == null) {
            throw new NullPointerException("messageId");
        }
        
        if (contact == null) {
            throw new NullPointerException("contact");
        }
        
        this.messageId = messageId;
        this.contact = contact;
        this.time = time;
        this.address = address;
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

    @Override
    public long getRemoteTime() {
        return time;
    }
    
    @Override
    public InetAddress getAddress() {
        return address;
    }
}
