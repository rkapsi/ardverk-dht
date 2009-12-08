package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;

public abstract class AbstractMessage implements Message {

    private final long creationTime = System.currentTimeMillis();
    
    private final MessageId messageId;
    
    private final Contact contact;
    
    private final SocketAddress address;
    
    public AbstractMessage(MessageId messageId, Contact source, 
            SocketAddress address) {
        
        if (messageId == null) {
            throw new NullPointerException("messageId");
        }
        
        if (source == null) {
            throw new NullPointerException("source");
        }
        
        if (address == null) {
            throw new NullPointerException("address");
        }
        
        this.messageId = messageId;
        this.contact = source;
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
}
