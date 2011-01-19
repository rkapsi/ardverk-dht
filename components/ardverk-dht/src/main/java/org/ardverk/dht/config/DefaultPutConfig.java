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

package org.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

import org.ardverk.dht.ExecutorKey;


public class DefaultPutConfig extends AbstractConfig implements PutConfig {

    private volatile LookupConfig lookupConfig = new DefaultLookupConfig();
    
    private volatile StoreConfig storeConfig = new DefaultStoreConfig();
    
    @Override
    public void setExecutorKey(ExecutorKey executorKey) {
        super.setExecutorKey(executorKey);
        lookupConfig.setExecutorKey(executorKey);
        storeConfig.setExecutorKey(executorKey);
    }
    
    @Override
    public LookupConfig getLookupConfig() {
        return lookupConfig;
    }
    
    @Override
    public void setLookupConfig(LookupConfig lookupConfig) {
        this.lookupConfig = lookupConfig;
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
    public void setOperationTimeout(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getOperationTimeout(TimeUnit unit) {
        return ConfigUtils.getOperationTimeout(new Config[] { lookupConfig, storeConfig }, unit);
    }
}