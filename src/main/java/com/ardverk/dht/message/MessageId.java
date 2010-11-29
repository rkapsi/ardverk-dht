package com.ardverk.dht.message;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import org.ardverk.coding.CodingUtils;
import org.ardverk.io.Writable;
import org.ardverk.lang.NullArgumentException;
import org.ardverk.security.SecurityUtils;
import org.ardverk.utils.ByteArrayComparator;

public final class MessageId implements Writable, Serializable, 
        Comparable<MessageId>, Cloneable {

    private static final long serialVersionUID = 6653397095695641792L;
    
    private static final Random GENERATOR 
        = SecurityUtils.createSecureRandom();
    
    public static MessageId createRandom(int length) {
        byte[] key = new byte[length];
        GENERATOR.nextBytes(key);
        return new MessageId(key);
    }
    
    public static MessageId createRandom(MessageId otherId) {
        return createRandom(otherId.length());
    }
    
    public static MessageId create(byte[] key) {
        return new MessageId(key);
    }
    
    public static MessageId create(byte[] key, int offset, int length) {
        byte[] copy = new byte[length];
        System.arraycopy(key, 0, copy, 0, copy.length);
        return new MessageId(copy);
    }
    
    public static MessageId create(BigInteger key) {
        return create(key.toByteArray());
    }
    
    public static MessageId create(String key, int radix) {
        return create(new BigInteger(key, radix));
    }
    
    private final byte[] messageId;
    
    private final int hashCode;
    
    private MessageId(byte[] messageId) {
        if (messageId == null) {
            throw new NullArgumentException("messageId");
        }
        
        this.messageId = messageId;
        this.hashCode = Arrays.hashCode(messageId);
    }
    
    /**
     * Returns {@code true} if the given {@link MessageId} is 
     * compatible with this {@link MessageId}.
     */
    public boolean isCompatible(MessageId otherId) {
        return otherId != null && length() == otherId.length();
    }

    @Override
    public int compareTo(MessageId o) {
        return ByteArrayComparator.COMPARATOR.compare(messageId, o.messageId);
    }

    @Override
    public int write(OutputStream out) throws IOException {
        out.write(messageId);
        return messageId.length;
    }

    public byte[] getBytes() {
        return messageId.clone();
    }

    public byte[] getBytes(byte[] dst, int destPos) {
        System.arraycopy(messageId, 0, dst, destPos, messageId.length);
        return dst;
    }
    
    public int length() {
        return messageId.length;
    }
    
    @Override
    public MessageId clone() {
        return this;
    }
    
    @Override
    public int hashCode() {
        return hashCode;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof MessageId)) {
            return false;
        }
        
        MessageId other = (MessageId)o;
        return Arrays.equals(messageId, other.messageId);
    }
    
    public String toHexString() {
        return CodingUtils.encodeBase16(messageId);
    }
    
    @Override
    public String toString() {
        return toHexString();
    }
}
