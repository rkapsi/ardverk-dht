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

package com.ardverk.dht.storage;

import com.ardverk.dht.config.StoreConfig;

/**
 * The {@link DatabaseConfig} defines a few basic settings that are 
 * needed by the DHT.
 */
public interface DatabaseConfig {

    /**
     * Returns {@code true} if store-forwarding is enabled.
     */
    public boolean isStoreForward();
    
    /**
     * Sets weather or not store-forwarding is enabled.
     */
    public void setStoreForward(boolean storeForward);
    
    /**
     * Returns {@code true} if bucket checking is enabled.
     */
    public boolean isCheckBucket();
    
    /**
     * Sets weather or not bucket checking is enabled.
     */
    public void setCheckBucket(boolean checkBucket);
    
    /**
     * Returns the {@link StoreConfig} that is used for store-forwarding.
     */
    public StoreConfig getStoreConfig();

    /**
     * Sets the {@link StoreConfig} that is used for store-forwarding.
     */
    public void setStoreConfig(StoreConfig storeConfig);
}