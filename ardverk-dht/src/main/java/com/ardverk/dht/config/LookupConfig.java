package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.routing.Contact;

/**
 * The {@link LookupConfig} is providing configuration data
 * for the lookup process.
 */
public interface LookupConfig extends Config {
    
    /**
     * Returns {@code true} if a lookup is exhaustive.
     */
    public boolean isExhaustive();

    /**
     * Sets whether or not the lookup is exhaustive.
     */
    public void setExhaustive(boolean exhaustive);

    /**
     * Returns {@code true} if the lookup process should pick a 
     * {@link Contact} by random out of the set of the k-closest.
     */
    public boolean isRandomize();

    /**
     * Sets whether or not the lookup process should pick {@link Contact}s
     * by random.
     */
    public void setRandomize(boolean randomize);

    /**
     * Returns the number of lookups that should be maintained in parallel.
     */
    public int getAlpha();

    /**
     * Sets the number of lookups that should be maintained in parallel.
     */
    public void setAlpha(int alpha);
    
    /**
     * Returns the boost frequency in the given {@link TimeUnit}.
     */
    public long getBoostFrequency(TimeUnit unit);
    
    /**
     * Returns the boost frequency in milliseconds.
     */
    public long getBoostFrequencyInMillis();
    
    /**
     * Sets the boost frequency. Negative values disable boosting.
     */
    public void setBoostFrequency(long boostFrequency, TimeUnit unit);
    
    /**
     * Returns the period of time that for which we must not have received
     * any responses before boosting is taken into consideration.
     */
    public long getBoostTimeout(TimeUnit unit);
    
    /**
     * Returns the period of time that for which we must not have received
     * any responses before boosting is taken into consideration.
     */
    public long getBoostTimeoutInMillis();
    
    /**
     * Sets the period of time after which boosting is taken into consideration.
     * 
     * @see #setBoostFrequency(long, TimeUnit)
     */
    public void setBoostTimeout(long boostTimeout, TimeUnit unit);
    
    /**
     * Returns the timeout that should be use used per lookup request
     * in the given {@link TimeUnit}.
     */
    public long getLookupTimeout(TimeUnit unit);
    
    /**
     * Returns the timeout that should be use used per lookup request
     * in milliseconds.
     */
    public long getLookupTimeoutInMillis();
    
    /**
     * Sets the timeout that should be used per lookup request.
     */
    public void setLookupTimeout(long timeout, TimeUnit unit);
}