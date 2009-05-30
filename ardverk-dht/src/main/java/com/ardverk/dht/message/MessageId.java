package com.ardverk.dht.message;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

public interface MessageId extends Serializable {

    public int length();
    
    public byte[] getBytes();
    
    public byte[] getBytes(byte[] dst, int destPos);
    
    public void write(OutputStream out) throws IOException;
}
