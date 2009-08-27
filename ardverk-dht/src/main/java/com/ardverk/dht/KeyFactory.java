package com.ardverk.dht;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public interface KeyFactory extends Serializable {

    public KUID createKey(byte[] key);
    
    public KUID createRandomKey();

    public KUID createKey(InputStream in) throws IOException;

    public KUID createKey(ByteBuffer in);
    
    public KUID createKey(String value, int radix);
    
    public KUID createKey(BigInteger value);
    
    public KUID min();

    public KUID max();

    public int length();
    
    public int lengthInBits();
}
