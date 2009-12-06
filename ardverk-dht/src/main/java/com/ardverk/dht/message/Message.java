package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact;

public interface Message {

    public long getCreationTime();
    
    public MessageId getMessageId();
    
    public Contact getContact();
}
