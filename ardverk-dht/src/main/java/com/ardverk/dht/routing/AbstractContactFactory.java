package com.ardverk.dht.routing;

import java.net.SocketAddress;

import com.ardverk.dht.KUID;
import com.ardverk.dht.KeyFactory;

public abstract class AbstractContactFactory implements ContactFactory {
    
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
    
    @Override
    public Contact createUnsolicited(KUID contactId, 
            int instanceId, SocketAddress address)  {
        return createUnsolicited(contactId, instanceId, address, null);
    }
    
    @Override
    public Contact createSolicited(KUID contactId, 
            int instanceId, SocketAddress address)  {
        return createSolicited(contactId, instanceId, address, null);
    }
    
    @Override
    public Contact createUnknown(KUID contactId, 
            int instanceId, SocketAddress address) {
        return createUnknown(contactId, instanceId, address, null);
    }
}
