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

public interface Config {
    
    /**
     * Returns the {@link QueueKey}.
     */
    public QueueKey getQueueKey();
    
    /**
     * Sets the {@link QueueKey}.
     */
    public void setQueueKey(QueueKey queueKey);
    
    /**
     * Sets the timeout for the operation
     */
    public void setOperationTimeout(long timeout, TimeUnit unit);
    
    /**
     * Returns the operation timeout in the given {@link TimeUnit}.
     */
    public long getOperationTimeout(TimeUnit unit);
    
    /**
     * Returns the operation timeout in milliseconds.
     */
    public long getOperationTimeoutInMillis();
    
    /**
     * The RTT multiplier is used multiply a {@link Contact}'s RTT
     * by some number.
     * 
     * @see #getAdaptiveTimeout(Contact, long, TimeUnit)
     */
    public void setRountTripTimeMultiplier(double multiplier);
    
    /**
     * @see #getAdaptiveTimeout(Contact, long, TimeUnit)
     */
    public double getRoundTripTimeMultiplier();
    
    /**
     * Returns an <tt>adaptive</tt> timeout for the given {@link Contact}
     * which is ideally less than the given default timeout.
     */
    public long getAdaptiveTimeout(Contact dst, long defaultTimeout, TimeUnit unit);
}