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


public class DefaultBootstrapConfig extends AbstractConfig 
        implements BootstrapConfig {

    private volatile PingConfig pingConfig = new DefaultPingConfig();
    
    private volatile LookupConfig lookupConfig = new DefaultLookupConfig();
    
    @Override
    public void setExecutorKey(ExecutorKey executorKey) {
        super.setExecutorKey(executorKey);
        pingConfig.setExecutorKey(executorKey);
        lookupConfig.setExecutorKey(executorKey);
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
    public LookupConfig getLookupConfig() {
        return lookupConfig;
    }
    
    @Override
    public void setLookupConfig(LookupConfig lookupConfig) {
        this.lookupConfig = lookupConfig;
    }

    @Override
    public void setOperationTimeout(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getOperationTimeout(TimeUnit unit) {
        return ConfigUtils.getOperationTimeout(new Config[] { pingConfig, lookupConfig }, unit);
    }
}