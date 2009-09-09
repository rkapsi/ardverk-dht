package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact;

public class AbstractResponseMessage extends AbstractMessage 
        implements ResponseMessage {

    public AbstractResponseMessage(OpCode opcode, 
            MessageId messageId, Contact contact) {
        super(opcode, messageId, contact);
    }

    
}
