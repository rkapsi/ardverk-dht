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

import org.ardverk.utils.TimeUtils;

public class DefaultPingConfig extends DefaultConfig 
        implements PingConfig {
    
    private static final long DEFAULT_PING_TIMEOUT 
        = TimeUtils.convert(10L, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
    
    public DefaultPingConfig() {
        super(DEFAULT_PING_TIMEOUT, TimeUnit.MILLISECONDS);
    }
    
    public DefaultPingConfig(long pingTimeout, TimeUnit unit) {
        super(pingTimeout, unit);
    }

    @Override
    public void setPingTimeout(long timeout, TimeUnit unit) {
        setOperationTimeout(timeout, unit);
    }

    @Override
    public long getPingTimeout(TimeUnit unit) {
        return getOperationTimeout(unit);
    }

    @Override
    public long getPingTimeoutInMillis() {
        return getOperationTimeoutInMillis();
    }
}