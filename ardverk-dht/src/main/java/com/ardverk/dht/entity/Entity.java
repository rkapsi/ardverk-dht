package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

public interface Entity {

    /**
     * Returns the Round Trip Time (RTT) in the given {@link TimeUnit}
     */
    public long getTime(TimeUnit unit);
    
    /**
     * Returns the Round Trip Time (RTT) in milliseconds
     */
    public long getTimeInMillis();
}
