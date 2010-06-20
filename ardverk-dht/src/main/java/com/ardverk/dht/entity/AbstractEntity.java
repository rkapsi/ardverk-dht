package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import org.ardverk.lang.Arguments;

public abstract class AbstractEntity implements Entity {

    protected final long time;
    
    protected final TimeUnit unit;
    
    public AbstractEntity(long time, TimeUnit unit) {
        this.time = Arguments.notNegative(time, "time");
        this.unit = Arguments.notNull(unit, "unit");
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
