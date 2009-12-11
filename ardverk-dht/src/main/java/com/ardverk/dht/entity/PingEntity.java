package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.message.PingResponse;
import com.ardverk.dht.routing.Contact;

public class PingEntity extends AbstractEntity {

    private final PingResponse response;
    
    public PingEntity(PingResponse response, long time, TimeUnit unit) {
        super(time, unit);
        
        if (response == null) {
            throw new NullPointerException("response");
        }
        
        this.response = response;
    }
    
    /**
     * Returns the remote {@link Contact}
     */
    public Contact getContact() {
        return response.getContact();
    }
    
    @Override
    public String toString() {
        return getContact() + ", " + time + ", " + unit;
    }
}
