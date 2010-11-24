package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

public interface LookupConfig extends Config {
    
    public boolean isExhaustive();

    public void setExhaustive(boolean exhaustive);

    public boolean isRandomize();

    public void setRandomize(boolean randomize);

    public int getAlpha();

    public void setAlpha(int alpha);
    
    public long getBoostFrequency(TimeUnit unit);
    
    public long getBoostFrequencyInMillis();
    
    public void setBoostFrequency(long boostFrequency, TimeUnit unit);
    
    public long getBoostTimeout(TimeUnit unit);
    
    public long getBoostTimeoutInMillis();
    
    public void setBoostTimeout(long boostTimeout, TimeUnit unit);
    
    public long getFooTimeout(TimeUnit unit);
    
    public long getFooTimeoutInMillis();
    
    public void setFooTimeout(long timeout, TimeUnit unit);
}