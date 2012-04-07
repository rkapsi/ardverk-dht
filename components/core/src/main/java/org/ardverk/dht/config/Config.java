/*
 * Copyright 2009-2012 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

import org.ardverk.dht.concurrent.ExecutorKey;
import org.ardverk.dht.routing.Contact;


public abstract class Config {

    private volatile double adaptiveTimeoutMultiplier = -1;
    
    private volatile ExecutorKey executorKey = ExecutorKey.DEFAULT;
    
    private volatile long operationTimeoutInMillis;
    
    public Config() {
    }
    
    public Config(long timeout, TimeUnit unit) {
        setOperationTimeout(timeout, unit);
    }
    
    public void setOperationTimeout(long timeout, TimeUnit unit) {
        this.operationTimeoutInMillis = unit.toMillis(timeout);
    }
    
    public long getOperationTimeout(TimeUnit unit) {
        return unit.convert(operationTimeoutInMillis, TimeUnit.MILLISECONDS);
    }
    
    public ExecutorKey getExecutorKey() {
        return executorKey;
    }

    public void setExecutorKey(ExecutorKey executorKey) {
        this.executorKey = executorKey;
    }

    public final long getOperationTimeoutInMillis() {
        return getOperationTimeout(TimeUnit.MILLISECONDS);
    }
    
    public double getRoundTripTimeMultiplier() {
        return adaptiveTimeoutMultiplier;
    }

    public void setRountTripTimeMultiplier(double adaptiveTimeoutMultiplier) {
        this.adaptiveTimeoutMultiplier = adaptiveTimeoutMultiplier;
    }
    
    public long getAdaptiveTimeout(Contact dst, 
            long defaultTimeout, TimeUnit unit) {
        double multiplier = getRoundTripTimeMultiplier();
        return ConfigUtils.getAdaptiveTimeout(dst, 
                multiplier, defaultTimeout, unit);
    }
}