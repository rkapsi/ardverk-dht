package com.ardverk.dht.message;

import java.net.SocketAddress;
import java.util.Random;

import org.ardverk.lang.Arguments;
import org.ardverk.security.SecurityUtils;

public abstract class AbstractMessageFactory implements MessageFactory {

    private static final Random GENERATOR = SecurityUtils.createSecureRandom();
    
    private final int length;
    
    public AbstractMessageFactory(int length) {
        this.length = Arguments.notNegative(length, "length");
    }
    
    @Override
    public MessageId createMessageId(SocketAddress dst) {
        byte[] messageId = new byte[length];
        GENERATOR.nextBytes(messageId);
        return MessageId.create(messageId);
    }

    @Override
    public boolean isFor(MessageId messageId, SocketAddress src) {
        return true;
    }
}
