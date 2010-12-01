package com.ardverk.dht.io;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;
import com.ardverk.dht.lang.ArdverkException;

/**
 * 
 */
public class PingTimeoutException extends ArdverkException {
    
    private static final long serialVersionUID = -7330783412590437071L;

    private final RequestEntity entity;
    
    public PingTimeoutException(RequestEntity entity, 
            long time, TimeUnit unit) {
        super (time, unit);
        this.entity = entity;
    }
    
    /**
     * 
     */
    public RequestEntity getRequestEntity() {
        return entity;
    }
    
    public KUID getContactId() {
        return entity.getContactId();
    }
    
    public SocketAddress getAddress() {
        return entity.getAddress();
    }
}
