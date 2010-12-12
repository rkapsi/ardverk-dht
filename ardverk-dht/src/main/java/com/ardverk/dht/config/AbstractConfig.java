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

import com.ardverk.dht.QueueKey;
import com.ardverk.dht.routing.Contact;

public abstract class AbstractConfig implements Config {

    private volatile double adaptiveTimeoutMultiplier = -1;
    
    private volatile QueueKey queueKey = QueueKey.DEFAULT;
    
    @Override
    public QueueKey getQueueKey() {
        return queueKey;
    }

    @Override
    public void setQueueKey(QueueKey queueKey) {
        this.queueKey = queueKey;
    }

    @Override
    public final long getOperationTimeoutInMillis() {
        return getOperationTimeout(TimeUnit.MILLISECONDS);
    }
    
    @Override
    public double getRoundTripTimeMultiplier() {
        return adaptiveTimeoutMultiplier;
    }

    @Override
    public void setRountTripTimeMultiplier(double adaptiveTimeoutMultiplier) {
        this.adaptiveTimeoutMultiplier = adaptiveTimeoutMultiplier;
    }
    
    @Override
    public long getAdaptiveTimeout(Contact dst, 
            long defaultTimeout, TimeUnit unit) {
        double multiplier = getRoundTripTimeMultiplier();
        return dst.getAdaptiveTimeout(multiplier, defaultTimeout, unit);
    }
}