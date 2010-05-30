package com.ardverk.security;

/**
 * 
 */
public abstract class AbstractSecurityToken implements SecurityToken {

    @Override
    public byte[] getBytes() {
        return getBytes(new byte[length()], 0);
    }
}
