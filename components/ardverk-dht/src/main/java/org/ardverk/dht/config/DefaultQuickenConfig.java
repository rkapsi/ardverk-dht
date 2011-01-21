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

import java.util.concurrent.TimeUnit;

import org.ardverk.dht.ExecutorKey;
import org.ardverk.utils.TimeUtils;


public class DefaultQuickenConfig extends AbstractConfig implements QuickenConfig {

    private volatile PingConfig pingConfig = new DefaultPingConfig();

    private volatile LookupConfig lookupConfig = new DefaultLookupConfig();

    private volatile float pingCount = 1.0f;
    
    private volatile long contactTimeoutInMillis 
        = TimeUtils.convert(5L*60L, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
    
    private volatile long bucketTimeoutInMillis 
        = TimeUtils.convert(5L*60L, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
    
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
    public float getPingCount() {
        return pingCount;
    }
    
    @Override
    public void setPingCount(float pingCount) {
        this.pingCount = pingCount;
    }

    @Override
    public long getContactTimeout(TimeUnit unit) {
        return unit.convert(contactTimeoutInMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public long getContactTimeoutInMillis() {
        return getContactTimeout(TimeUnit.MILLISECONDS);
    }
    
    @Override
    public void setContactTimeout(long timeout, TimeUnit unit) {
        this.contactTimeoutInMillis = unit.toMillis(timeout);
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
    public long getBucketTimeout(TimeUnit unit) {
        return unit.convert(bucketTimeoutInMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public long getBucketTimeoutInMillis() {
        return getBucketTimeout(TimeUnit.MILLISECONDS);
    }
    
    @Override
    public void setBucketTimeout(long timeout, TimeUnit unit) {
        this.bucketTimeoutInMillis = unit.toMillis(timeout);
    }

    @Override
    public void setOperationTimeout(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getOperationTimeout(TimeUnit unit) {
        throw new UnsupportedOperationException();
    }
}