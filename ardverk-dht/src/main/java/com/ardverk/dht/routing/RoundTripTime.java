package com.ardverk.dht.routing;

import java.util.concurrent.TimeUnit;

public interface RoundTripTime {

    /**
     * Returns the {@link Contact}'s Round-Trip-Time (RTT) or a negative 
     * value if the RTT is unknown.
     */
    public long getRoundTripTime(TimeUnit unit);
    
    /**
     * Returns the {@link Contact}'s Round-Trip-Time (RTT) in milliseconds
     * or a negative value if the RTT is unknown.
     */
    public long getRoundTripTimeInMillis();
    
    /**
     * Changes the {@link Contact}'s Round-Trip-Time (RTT)
     */
    public void setRoundTripTime(long rtt, TimeUnit unit);
}
