package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact;

public abstract class AbstractRequestMessage extends AbstractMessage 
        implements RequestMessage {

    public AbstractRequestMessage(OpCode opcode, 
            MessageId messageId, Contact contact) {
        super(opcode, messageId, contact);
    }
}
