package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import org.ardverk.lang.NullArgumentException;

import com.ardverk.dht.message.PingResponse;
import com.ardverk.dht.routing.Contact;

public class DefaultPingEntity extends AbstractEntity implements PingEntity {
    
    private final PingResponse response;
    
    public DefaultPingEntity(PingResponse response, long time, TimeUnit unit) {
        super(time, unit);
        
        if (response == null) {
            throw new NullArgumentException("response");
        }
        
        this.response = response;
    }
    
    @Override
    public Contact getContact() {
        return response.getContact();
    }
    
    @Override
    public String toString() {
        return getContact() + ", " + time + ", " + unit;
    }
}
