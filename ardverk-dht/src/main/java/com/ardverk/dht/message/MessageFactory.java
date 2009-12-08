package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;

public interface MessageFactory {

    //public Message decode(SocketAddress src, 
    //        Object message) throws IOException;
    
    //public MessageId createMessageId(byte[] messageId);
    
    //public MessageId createMessageId(SessionContext context);
    
    public MessageId createMessageId(SocketAddress dst);
    
    public boolean isFor(MessageId messageId, SocketAddress src);
    
    public PingRequest createPingRequest(SocketAddress dst);
    
    public PingRequest createPingRequest(Contact dst);
}
