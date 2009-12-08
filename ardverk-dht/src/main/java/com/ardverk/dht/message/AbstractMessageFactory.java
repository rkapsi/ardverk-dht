package com.ardverk.dht.message;

import java.net.SocketAddress;
import java.security.SecureRandom;
import java.util.Random;

public abstract class AbstractMessageFactory implements MessageFactory {

    private static final Random GENERATOR = new SecureRandom();
    
    private final int length;
    
    public AbstractMessageFactory(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length=" + length);
        }
        
        this.length = length;
    }
    
    @Override
    public MessageId createMessageId(SocketAddress dst) {
        byte[] messageId = new byte[length];
        GENERATOR.nextBytes(messageId);
        return new MessageId(messageId);
    }

    @Override
    public boolean isFor(MessageId messageId, SocketAddress src) {
        return true;
    }
}
