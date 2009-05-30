package com.ardverk.dht.message;

import java.io.Serializable;
import java.net.SocketAddress;

import com.ardverk.io.Writable;

public interface MessageId extends Writable, Serializable, Comparable<MessageId> {

    public boolean isTaggable();
    
    public boolean isFor(SocketAddress address);
    
    public int length();
    
    public byte[] getBytes();
    
    public byte[] getBytes(byte[] dst, int destPos);
}
