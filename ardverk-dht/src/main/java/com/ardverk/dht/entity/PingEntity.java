package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.message.PingResponse;
import com.ardverk.dht.routing.Contact;

public class PingEntity extends AbstractEntity {

    private final PingResponse response;
    
    private final long time;
    
    private final TimeUnit unit;
    
    public PingEntity(PingResponse response, long time, TimeUnit unit) {
        if (response == null) {
            throw new NullPointerException("response");
        }
        
        if (time < 0L) {
            throw new IllegalArgumentException("time=" + time);
        }
        
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        
        this.response = response;
        this.time = time;
        this.unit = unit;
    }
    
    public Contact getContact() {
        return response.getContact();
    }
    
    public long getTime(TimeUnit unit) {
        return unit.convert(time, this.unit);
    }
    
    @Override
    public String toString() {
        return getContact() + ", " + time + ", " + unit;
    }
}
