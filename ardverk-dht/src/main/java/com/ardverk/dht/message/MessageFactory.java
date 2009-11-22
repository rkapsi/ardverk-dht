package com.ardverk.dht.message;

import com.ardverk.dht.io.session.SessionContext;

public interface MessageFactory {

    public MessageId createMessageId(byte[] messageId);
    
    public MessageId createMessageId(SessionContext context);
    
    public boolean isFor(MessageId messageId, SessionContext context);
}
