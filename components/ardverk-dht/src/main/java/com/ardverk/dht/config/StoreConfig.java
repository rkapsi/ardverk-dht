/*
 * Copyright 2009-2010 Roger Kapsi
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

import com.ardverk.dht.message.MessageType;

/**
 * The {@link StoreConfig} provides configuration data for the 
 * {@link MessageType#STORE} operation.
 */
public interface StoreConfig extends Config {
    
    /**
     * Returns the network timeout in the given {@link TimeUnit}.
     */
    public long getStoreTimeout(TimeUnit unit);
    
    /**
     * Returns the network timeout in milliseconds.
     */
    public long getStoreTimeoutInMillis();
    
    /**
     * Sets the network timeout.
     */
    public void setStoreTimeout(long timeout, TimeUnit unit);
    
    /**
     * The S parameter is controlling how many {@link MessageType#STORE} 
     * operations should run in parallel.
     */
    public int getS();
    
    /**
     * The S parameter is controlling how many {@link MessageType#STORE}
     * operations should run in parallel.
     */
    public void setS(int value);
}