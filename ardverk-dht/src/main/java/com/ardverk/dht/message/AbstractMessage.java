package com.ardverk.dht.message;

import java.net.SocketAddress;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.routing.Contact;

public abstract class AbstractMessage implements Message {

    private final long creationTime = System.currentTimeMillis();
    
    private final MessageId messageId;
    
    private final Contact contact;
    
    private final SocketAddress address;
    
    public AbstractMessage(MessageId messageId, 
            Contact contact, SocketAddress address) {
        
        this.messageId = Arguments.notNull(messageId, "messageId");
        this.contact = Arguments.notNull(contact, "contact");
        this.address = Arguments.notNull(address, "address");
    }
    
    /**
     * Returns the {@link Message}'s creation time.
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
        return getClass().getSimpleName() 
            + "(" + messageId + ", " + contact + ", " + address + ")";
    }
}
