package com.ardverk.dht.message;

import java.io.Serializable;
import java.net.InetSocketAddress;

import com.ardverk.io.Writable;

public interface MessageId extends Writable, Serializable, Comparable<MessageId> {

    public boolean isTaggable();
    
    public boolean isFor(InetSocketAddress address);
    
    public int length();
    
    public byte[] getBytes();
    
    public byte[] getBytes(byte[] dst, int destPos);
}
