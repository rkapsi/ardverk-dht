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

import com.ardverk.dht.routing.Bucket;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;

/**
 * The {@link QuickenConfig} provides configuration data for the
 * quicken operation.
 */
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
    
    /**
     * The ping count is used to determinate how many {@link Contact}s 
     * should be selected from the {@link RouteTable}.
     * 
     * selectCount = ping count * K
     */
    public float getPingCount();
    
    /**
     * Sets how many {@link Contact}s should be selected from 
     * the {@link RouteTable}.
     */
    public void setPingCount(float pingCount);
    
    /**
     * The {@link Contact} timeout is used to determinate if it's necessary
     * to send a PING to a {@link Contact}.
     */
    public long getContactTimeout(TimeUnit unit);
    
    /**
     * The {@link Contact} timeout is used to determinate if it's necessary
     * to send a PING to a {@link Contact}.
     */
    public long getContactTimeoutInMillis();
    
    /**
     * The {@link Contact} timeout is used to determinate if it's necessary
     * to send a PING to a {@link Contact}.
     */
    public void setContactTimeout(long timeout, TimeUnit unit);
    
    /**
     * The {@link Bucket} timeout is used to determinate if it's necessary
     * to refresh a {@link Bucket}.
     */
    public long getBucketTimeout(TimeUnit unit);
    
    /**
     * The {@link Bucket} timeout is used to determinate if it's necessary
     * to refresh a {@link Bucket}.
     */
    public long getBucketTimeoutInMillis();
    
    /**
     * The {@link Bucket} timeout is used to determinate if it's necessary
     * to refresh a {@link Bucket}.
     */
    public void setBucketTimeout(long timeout, TimeUnit unit);
}