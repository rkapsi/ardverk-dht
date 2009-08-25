package com.ardverk.dht.message;

import java.net.InetSocketAddress;

public interface MessageFactory {

    public MessageId createMessageId(byte[] messageId);
    
    public MessageId createMessageId(InetSocketAddress dst);
    
    public boolean isFor(MessageId messageId, InetSocketAddress src);
}
