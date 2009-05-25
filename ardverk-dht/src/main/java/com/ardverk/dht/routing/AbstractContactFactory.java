package com.ardverk.dht.routing;

import com.ardverk.dht.KeyFactory;

public abstract class AbstractContactFactory implements ContactFactory {
    
    private static final long serialVersionUID = 5916104248888312047L;
    
    protected final KeyFactory keyFactory;
    
    public AbstractContactFactory(KeyFactory keyFactory) {
        if (keyFactory == null) {
            throw new NullPointerException("keyFactory");
        }
        
        this.keyFactory = keyFactory;
    }
    
    @Override
    public KeyFactory getKeyFactory() {
        return keyFactory;
    }
}
