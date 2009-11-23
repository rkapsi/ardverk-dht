package com.ardverk.dht.message;

import java.io.IOException;
import java.net.SocketAddress;

import com.ardverk.dht.io.session.SessionContext;

public interface MessageFactory {

    public Message decode(SocketAddress src, 
            Object message) throws IOException;
    
    public MessageId createMessageId(byte[] messageId);
    
    public MessageId createMessageId(SessionContext context);
    
    public boolean isFor(MessageId messageId, SessionContext context);
}
