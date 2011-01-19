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

package org.ardverk.dht.routing;

import java.util.concurrent.TimeUnit;

import org.ardverk.dht.ExecutorKey;
import org.ardverk.dht.config.DefaultPingConfig;
import org.ardverk.dht.config.PingConfig;
import org.ardverk.net.NetworkMask;
import org.ardverk.utils.TimeUtils;


public class RouteTableConfig {
    
    public static final int DEFAULT_K = 20;
    
    private final int k;
    
    private volatile PingConfig pingConfig = new DefaultPingConfig();
    
    private volatile int maxDepth = Integer.MAX_VALUE;
    
    private volatile int maxCacheSize = 16;
        
    private volatile NetworkMask networkMask = NetworkMask.C;
    
    private volatile double probability = 0.75d;
    
    private volatile int maxConsecutiveErrors = 100;

    private volatile int maxContactsFromSameNetwork = -1;
    
    private volatile int tooManyErrorsCount = Integer.MAX_VALUE;
    
    private volatile boolean checkIdentity = true;
    
    private volatile int maxContactErrors = 5;
    
    private volatile long hasBeenActiveTimeoutInMillis 
        = TimeUtils.convert(5L*60L, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
    
    // INIT
    {
        pingConfig.setExecutorKey(ExecutorKey.BACKEND);
    }
    
    public RouteTableConfig() {
        this(DEFAULT_K);
    }
    
    public RouteTableConfig(int k) {
        this.k = k;
    }
    
    public int getK() {
        return k;
    }
    
    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    public NetworkMask getNetworkMask() {
        return networkMask;
    }

    public void setNetworkMask(NetworkMask networkMask) {
        this.networkMask = networkMask;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public int getMaxConsecutiveErrors() {
        return maxConsecutiveErrors;
    }

    public void setMaxConsecutiveErrors(int maxConsecutiveErrors) {
        this.maxConsecutiveErrors = maxConsecutiveErrors;
    }

    public int getMaxContactsFromSameNetwork() {
        return maxContactsFromSameNetwork;
    }

    public void setMaxContactsFromSameNetwork(int maxContactsFromSameNetwork) {
        this.maxContactsFromSameNetwork = maxContactsFromSameNetwork;
    }

    public int getTooManyErrorsCount() {
        return tooManyErrorsCount;
    }

    public void setTooManyErrorsCount(int tooManyErrorsCount) {
        this.tooManyErrorsCount = tooManyErrorsCount;
    }

    public boolean isCheckIdentity() {
        return checkIdentity;
    }

    public void setCheckIdentity(boolean checkIdentity) {
        this.checkIdentity = checkIdentity;
    }

    public PingConfig getPingConfig() {
        return pingConfig;
    }

    public void setPingConfig(PingConfig pingConfig) {
        this.pingConfig = pingConfig;
    }

    public int getMaxContactErrors() {
        return maxContactErrors;
    }

    public void setMaxContactErrors(int maxContactErrors) {
        this.maxContactErrors = maxContactErrors;
    }
    
    public void setHasBeenActiveTimeout(long foo, TimeUnit unit) {
        this.hasBeenActiveTimeoutInMillis = unit.toMillis(foo);
    }
    
    public long getHasBeenActiveTimeout(TimeUnit unit) {
        return unit.convert(hasBeenActiveTimeoutInMillis, TimeUnit.MILLISECONDS);
    }
    
    public long getHasBeenActiveTimeoutInMillis() {
        return getHasBeenActiveTimeout(TimeUnit.MILLISECONDS);
    }
}