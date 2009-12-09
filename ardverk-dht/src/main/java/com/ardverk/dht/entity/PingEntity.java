package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

public class PingEntity extends AbstractEntity {

    private final long time;
    
    private final TimeUnit unit;
    
    public PingEntity(long time, TimeUnit unit) {
        if (time < 0L) {
            throw new IllegalArgumentException("time=" + time);
        }
        
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        
        this.time = time;
        this.unit = unit;
    }
    
    public long getTime(TimeUnit unit) {
        return unit.convert(time, this.unit);
    }
    
    @Override
    public String toString() {
        return time + ", " + unit;
    }
}
