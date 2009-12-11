package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

public abstract class AbstractEntity implements Entity {

    protected final long time;
    
    protected final TimeUnit unit;
    
    public AbstractEntity(long time, TimeUnit unit) {
        if (time < 0L) {
            throw new IllegalArgumentException("time=" + time);
        }
        
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        
        this.time = time;
        this.unit = unit;
    }
    
    @Override
    public long getTime(TimeUnit unit) {
        return unit.convert(time, this.unit);
    }
    
    @Override
    public long getTimeInMillis() {
        return getTime(TimeUnit.MILLISECONDS);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " (" + time + ", " + unit + ")";
    }
}
