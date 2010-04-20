package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.message.PingResponse;
import com.ardverk.dht.routing.Contact2;

public class DefaultPingEntity extends AbstractEntity implements PingEntity {
    
    private final PingResponse response;
    
    public DefaultPingEntity(PingResponse response, long time, TimeUnit unit) {
        super(time, unit);
        
        if (response == null) {
            throw new NullPointerException("response");
        }
        
        this.response = response;
    }
    
    @Override
    public Contact2 getContact() {
        return response.getContact();
    }
    
    @Override
    public String toString() {
        return getContact() + ", " + time + ", " + unit;
    }
}
