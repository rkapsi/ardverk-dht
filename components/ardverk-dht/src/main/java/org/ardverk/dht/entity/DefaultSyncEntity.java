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
 * A default implementation of {@link SyncEntity}.
 */
public class DefaultSyncEntity extends AbstractEntity implements SyncEntity {

    private final ArdverkFuture<StoreEntity>[] futures;
    
    @SuppressWarnings("unchecked")
    public DefaultSyncEntity(long time, TimeUnit unit) {
        this(new ArdverkFuture[0], time, unit);
    }
    
    public DefaultSyncEntity(ArdverkFuture<StoreEntity>[] futures, 
            long time, TimeUnit unit) {
        super(time, unit);
        this.futures = futures;
    }

    @Override
    public ArdverkFuture<StoreEntity>[] getStoreFutures() {
        return futures;
    }
}