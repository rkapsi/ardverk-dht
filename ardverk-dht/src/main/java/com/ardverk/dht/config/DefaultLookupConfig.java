package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

public class DefaultLookupConfig extends DefaultConfig 
        implements LookupConfig {
    
    private static final boolean EXHAUSTIVE = false;
    
    private static final boolean RANDOMIZE = false;
    
    private static final long BOOST_FREQUENCY = 1000L;
    
    private static final long BOOST_TIMEOUT = 3000L;
    
    private static final int ALPHA = 4;
    
    private volatile boolean exhaustive = EXHAUSTIVE;
    
    private volatile boolean randomize = RANDOMIZE;
    
    private volatile int alpha = ALPHA;

    private volatile long boostFrequency = BOOST_FREQUENCY;
    
    private volatile long boostTimeout = BOOST_TIMEOUT;
    
    private volatile long fooTimeoutInMillis = 3L * 1000L;
    
    public DefaultLookupConfig() {
        this(1L, TimeUnit.MINUTES);
    }
    
    public DefaultLookupConfig(long timeout, TimeUnit unit) {
        super(timeout, unit);
    }
    
    @Override
    public boolean isExhaustive() {
        return exhaustive;
    }

    @Override
    public void setExhaustive(boolean exhaustive) {
        this.exhaustive = exhaustive;
    }

    @Override
    public boolean isRandomize() {
        return randomize;
    }

    @Override
    public void setRandomize(boolean randomize) {
        this.randomize = randomize;
    }

    @Override
    public int getAlpha() {
        return alpha;
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }
    
    @Override
    public long getBoostFrequency(TimeUnit unit) {
        return unit.convert(boostFrequency, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public long getBoostFrequencyInMillis() {
        return getBoostFrequency(TimeUnit.MILLISECONDS);
    }
    
    @Override
    public void setBoostFrequency(long boostFrequency, TimeUnit unit) {
        this.boostFrequency = unit.toMillis(boostTimeout);
    }
    
    @Override
    public long getBoostTimeout(TimeUnit unit) {
        return unit.convert(boostTimeout, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public long getBoostTimeoutInMillis() {
        return getBoostTimeout(TimeUnit.MILLISECONDS);
    }
    
    @Override
    public void setBoostTimeout(long boostTimeout, TimeUnit unit) {
        this.boostTimeout = unit.toMillis(boostTimeout);
    }

    @Override
    public long getFooTimeout(TimeUnit unit) {
        return unit.convert(fooTimeoutInMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public long getFooTimeoutInMillis() {
        return getFooTimeout(TimeUnit.MILLISECONDS);
    }

    @Override
    public void setFooTimeout(long timeout, TimeUnit unit) {
        this.fooTimeoutInMillis = unit.toMillis(timeout);
    }
}