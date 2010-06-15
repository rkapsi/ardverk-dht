package com.ardverk.dht.settings;

import java.util.concurrent.TimeUnit;

public class LookupSettings {

    private static final boolean EXHAUSTIVE = false;
    
    private static final boolean RANDOMIZE = false;
    
    private static final long BOOST_FREQUENCY = 1000L;
    
    private static final long BOOST_TIMEOUT = 3000L;
    
    private static final int R = 1;

    private static final int ALPHA = 4;
    
    private volatile boolean exhaustive = EXHAUSTIVE;
    
    private volatile boolean randomize = RANDOMIZE;
    
    private volatile int r = R;
    
    private volatile int alpha = ALPHA;

    private volatile long boostFrequency = BOOST_FREQUENCY;
    
    private volatile long boostTimeout = BOOST_TIMEOUT;
    
    public boolean isExhaustive() {
        return exhaustive;
    }

    public void setExhaustive(boolean exhaustive) {
        this.exhaustive = exhaustive;
    }

    public boolean isRandomize() {
        return randomize;
    }

    public void setRandomize(boolean randomize) {
        this.randomize = randomize;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }
    
    public long getBoostFrequency(TimeUnit unit) {
        return unit.convert(boostFrequency, TimeUnit.MILLISECONDS);
    }
    
    public long getBoostFrequencyInMillis() {
        return getBoostFrequency(TimeUnit.MILLISECONDS);
    }
    
    public void setBoostFrequency(long boostFrequency, TimeUnit unit) {
        this.boostFrequency = unit.toMillis(boostTimeout);
    }
    
    public long getBoostTimeout(TimeUnit unit) {
        return unit.convert(boostTimeout, TimeUnit.MILLISECONDS);
    }
    
    public long getBoostTimeoutInMillis() {
        return getBoostTimeout(TimeUnit.MILLISECONDS);
    }
    
    public void setBoostTimeout(long boostTimeout, TimeUnit unit) {
        this.boostTimeout = unit.toMillis(boostTimeout);
    }
}
