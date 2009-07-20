package com.ardverk.coding;

public interface BaseCoder {

    public byte[] encode(byte[] data);
    
    public byte[] encode(byte[] data, int offset, int length);
    
    public byte[] decode(byte[] data);
    
    public byte[] decode(byte[] data, int offset, int length);
}
