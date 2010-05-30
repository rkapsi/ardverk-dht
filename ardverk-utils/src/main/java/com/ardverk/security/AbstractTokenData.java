package com.ardverk.security;

import com.ardverk.security.TokenData;

/**
 * 
 */
public abstract class AbstractTokenData implements TokenData {

    @Override
    public byte[] getBytes() {
        return getBytes(new byte[length()], 0);
    }
}
