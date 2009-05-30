package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact;

public abstract class AbstractMessage implements Message {

    private final long creationTime = System.currentTimeMillis();

    private final OpCode opcode;
    
    private final MessageId messageId;
    
    private final Contact contact;
    
    public AbstractMessage(OpCode opcode, 
            MessageId messageId, Contact contact) {
        if (opcode == null) {
            throw new NullPointerException("opcode");
        }
        
        if (messageId == null) {
            throw new NullPointerException("messageId");
        }
        
        if (contact == null) {
            throw new NullPointerException("contact");
        }
        
        this.opcode = opcode;
        this.messageId = messageId;
        this.contact = contact;
    }
    
    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public OpCode getOpCode() {
        return opcode;
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
