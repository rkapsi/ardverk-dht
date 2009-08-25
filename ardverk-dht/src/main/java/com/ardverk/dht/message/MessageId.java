package com.ardverk.dht.message;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

import com.ardverk.io.Writable;
import com.ardverk.utils.ByteArrayComparator;

public class MessageId implements Writable, Serializable, Comparable<MessageId> {

    private static final long serialVersionUID = 6653397095695641792L;
    
    private final byte[] messageId;
    
    private final int hashCode;
    
    public MessageId(byte[] messageId) {
        if (messageId == null) {
            throw new NullPointerException("messageId");
        }
        
        this.messageId = messageId;
        this.hashCode = Arrays.hashCode(messageId);
    }

    @Override
    public int compareTo(MessageId o) {
        return ByteArrayComparator.COMPARATOR.compare(messageId, o.getBytes());
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
        return new BigInteger(1, messageId).toString(16);
    }
    
    @Override
    public String toString() {
        return toHexString();
    }
}
