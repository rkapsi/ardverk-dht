/*
 * Copyright 2009-2011 Roger Kapsi
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

package org.ardverk.dht.storage;

import org.ardverk.dht.concurrent.ExecutorKey;
import org.ardverk.dht.config.DefaultStoreConfig;
import org.ardverk.dht.config.StoreConfig;

/**
 * A default implementation of {@link DatastoreConfig}.
 */
public class DefaultDatastoreConfig implements DatastoreConfig {

    private volatile StoreConfig storeConfig = new DefaultStoreConfig();
    
    private volatile boolean checkBucket = false;
    
    private volatile boolean storeForwad = true;
    
    // INIT
    {
        storeConfig.setExecutorKey(ExecutorKey.BACKEND);
    }

    @Override
    public StoreConfig getStoreConfig() {
        return storeConfig;
    }

    @Override
    public void setStoreConfig(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
    }

    @Override
    public boolean isStoreForward() {
        return storeForwad;
    }

    @Override
    public void setStoreForward(boolean storeForwad) {
        this.storeForwad = storeForwad;
    }

    @Override
    public boolean isCheckBucket() {
        return checkBucket;
    }
    
    @Override
    public void setCheckBucket(boolean checkBucket) {
        this.checkBucket = checkBucket;
    }
}