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

package org.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import org.ardverk.dht.concurrent.ArdverkFuture;

/**
 * A default implementation of {@link QuickenEntity}.
 */
public class DefaultQuickenEntity extends AbstractEntity implements QuickenEntity {

    private final ArdverkFuture<PingEntity>[] pingFutures;
    
    private final ArdverkFuture<NodeEntity>[] lookupFutures;
    
    public DefaultQuickenEntity(ArdverkFuture<PingEntity>[] pingFutures, 
            ArdverkFuture<NodeEntity>[] lookupFutures, long time, TimeUnit unit) {
        super(time, unit);
        
        this.pingFutures = pingFutures;
        this.lookupFutures = lookupFutures;
    }

    @Override
    public ArdverkFuture<PingEntity>[] getPingFutures() {
        return pingFutures;
    }

    @Override
    public ArdverkFuture<NodeEntity>[] getLookupFutures() {
        return lookupFutures;
    }
}