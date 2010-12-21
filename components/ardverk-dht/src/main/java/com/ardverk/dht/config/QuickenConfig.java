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

public interface QuickenConfig extends Config {

    /**
     * Returns the {@link PingConfig} that's used for refreshing.
     */
    public PingConfig getPingConfig();
    
    /**
     * Sets the {@link PingConfig} that's used for refreshing.
     */
    public void setPingConfig(PingConfig pingConfig);
    
    /**
     * Returns the {@link LookupConfig} that's used for refreshing.
     */
    public LookupConfig getLookupConfig();
    
    /**
     * Returns the {@link LookupConfig} that's used for refreshing.
     */
    public void setLookupConfig(LookupConfig lookupConfig);
    
    public float getPingCount();
    
    public void setPingCount(float pingCount);
    
    public long getContactTimeout(TimeUnit unit);
    
    public long getContactTimeoutInMillis();
    
    public void setContactTimeout(long timeout, TimeUnit unit);
    
    public long getBucketTimeout(TimeUnit unit);
    
    public long getBucketTimeoutInMillis();
    
    public void setBucketTimeout(long timeout, TimeUnit unit);
}