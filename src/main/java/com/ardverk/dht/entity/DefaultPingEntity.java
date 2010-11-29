package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.message.PingResponse;
import com.ardverk.dht.routing.Contact;

public class DefaultPingEntity extends AbstractEntity implements PingEntity {
    
    private final PingResponse response;
    
    public DefaultPingEntity(PingResponse response, 
            long time, TimeUnit unit) {
        super(time, unit);
        this.response = response;
    }
    
    @Override
    public Contact getContact() {
        return response.getContact();
    }
    
    public PingResponse getPingResponse() {
        return response;
    }
    
    @Override
    public String toString() {
        return getContact() + ", " + time + ", " + unit;
    }
}
