package com.ardverk.security;

import org.ardverk.io.Writable;

/**
 * 
 */
public interface TokenData extends Writable {
    
    /**
     * 
     */
    public int length();
    
    /**
     * 
     */
    public byte[] getBytes();
    
    /**
     * 
     */
    public byte[] getBytes(byte[] dst, int offset);
}