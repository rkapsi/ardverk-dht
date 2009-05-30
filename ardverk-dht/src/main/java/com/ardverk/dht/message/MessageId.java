package com.ardverk.dht.message;

import java.io.Serializable;

import com.ardverk.io.Writable;

public interface MessageId extends Writable, Serializable {

    public int length();
    
    public byte[] getBytes();
    
    public byte[] getBytes(byte[] dst, int destPos);
}
