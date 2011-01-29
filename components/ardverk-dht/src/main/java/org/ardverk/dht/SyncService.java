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

package org.ardverk.dht;

import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.config.SyncConfig;
import org.ardverk.dht.entity.SyncEntity;
import org.ardverk.dht.routing.Contact;

/**
 * The {@link SyncService} provides methods to sync one DHT's values with
 * everyone else's in the same bucket.
 */
interface SyncService {

    /**
     * Synchronizes this {@link Contact}'s values with the k-closest.
     */
    public DHTFuture<SyncEntity> sync(SyncConfig config);
}