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

package org.ardverk.dht.config;

import org.ardverk.dht.concurrent.ExecutorKey;

public class DefaultSyncConfig implements SyncConfig {

    private volatile PingConfig pingConfig = new DefaultPingConfig();
    
    private volatile StoreConfig storeConfig = new DefaultStoreConfig();
    
    // INIT
    {
        pingConfig.setExecutorKey(ExecutorKey.BACKEND);
        storeConfig.setExecutorKey(ExecutorKey.BACKEND);
    }
    
    @Override
    public PingConfig getPingConfig() {
        return pingConfig;
    }
    
    @Override
    public void setPingConfig(PingConfig pingConfig) {
        this.pingConfig = pingConfig;
    }
    
    @Override
    public StoreConfig getStoreConfig() {
        return storeConfig;
    }
    
    @Override
    public void setStoreConfig(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
    }
}