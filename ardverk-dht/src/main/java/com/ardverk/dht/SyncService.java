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

package com.ardverk.dht;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.SyncConfig;
import com.ardverk.dht.entity.SyncEntity;
import com.ardverk.dht.routing.IContact;

public interface SyncService {

    /**
     * Synchronizes this {@link IContact}'s values with the k-closest.
     */
    public ArdverkFuture<SyncEntity> sync(SyncConfig config);
}