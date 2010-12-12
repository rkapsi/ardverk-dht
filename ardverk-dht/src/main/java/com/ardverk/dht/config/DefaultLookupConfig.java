/*
 * Copyright 2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

import org.ardverk.utils.TimeUtils;

public class DefaultLookupConfig extends DefaultConfig 
        implements LookupConfig {
    
    private static final long DEFAULT_OPERATION_TIMEOUT 
        = TimeUtils.convert(1L, TimeUnit.MINUTES, TimeUnit.MILLISECONDS);
    
    private volatile boolean exhaustive = false;
    
    private volatile boolean randomize = false;
    
    private volatile int alpha = 4;

    private volatile long boostFrequency 
        = TimeUtils.convert(5L, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
    
    private volatile long boostTimeout 
        = TimeUtils.convert(3L, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
    
    private volatile long lookupTimeoutInMillis 
        = TimeUtils.convert(10L, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
    
    public DefaultLookupConfig() {
        this(DEFAULT_OPERATION_TIMEOUT, TimeUnit.MINUTES);
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
    public long getLookupTimeout(TimeUnit unit) {
        return unit.convert(lookupTimeoutInMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public long getLookupTimeoutInMillis() {
        return getLookupTimeout(TimeUnit.MILLISECONDS);
    }

    @Override
    public void setLookupTimeout(long timeout, TimeUnit unit) {
        this.lookupTimeoutInMillis = unit.toMillis(timeout);
    }
}