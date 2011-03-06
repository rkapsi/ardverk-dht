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

import org.ardverk.utils.TimeUtils;

public class DefaultStoreConfig extends DefaultConfig 
        implements StoreConfig {
    
    private static final long DEFAULT_STORE_TIMEOUT
        = TimeUtils.convert(60L, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
    
    private volatile int s = 5;
    
    private volatile boolean sloppy = true;
    
    public DefaultStoreConfig() {
        super(DEFAULT_STORE_TIMEOUT, TimeUnit.MILLISECONDS);
    }
    
    public DefaultStoreConfig(long storeTimeout, TimeUnit unit) {
        super(storeTimeout, unit);
    }

    @Override
    public long getStoreTimeout(TimeUnit unit) {
        return getOperationTimeout(unit);
    }

    @Override
    public long getStoreTimeoutInMillis() {
        return getOperationTimeoutInMillis();
    }

    @Override
    public void setStoreTimeout(long timeout, TimeUnit unit) {
        setOperationTimeout(timeout, unit);
    }

    @Override
    public int getS() {
        return s;
    }

    @Override
    public void setS(int s) {
        this.s = s;
    }

    @Override
    public boolean isSloppy() {
        return sloppy;
    }

    @Override
    public void setSloppy(boolean sloppy) {
        this.sloppy = sloppy;
    }
}