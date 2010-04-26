package com.ardverk.dht.message;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;

import org.ardverk.io.Writable;
import org.ardverk.utils.ByteArrayComparator;

import com.ardverk.coding.CodingUtils;

public final class MessageId implements Writable, Serializable, Comparable<MessageId> {

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
