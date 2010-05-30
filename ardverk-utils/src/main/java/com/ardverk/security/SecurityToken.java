package com.ardverk.security;

import java.net.SocketAddress;

import org.ardverk.io.Writable;


/**
 * 
 */
public interface SecurityToken extends Writable {

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
    
    /**
     * 
     */
    public boolean isFor(SocketAddress src);
}
