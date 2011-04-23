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

package org.ardverk.dht.storage;

import java.util.Set;

import org.ardverk.dht.KUID;

/**
 * A simple and minimum interface of a {@link Database} 
 * that is needed by the DHT.
 */
public interface Database {
    
    /**
     * Returns the {@link DatabaseConfig}.
     */
    public DatabaseConfig getDatabaseConfig();
    
    /**
     * Stores the given {@link ValueTuple} and returns a {@link Status}.
     */
    public Status store(Resource resource);
    
    /**
     * Returns all bucket {@link KUID}s.
     */
    public Set<KUID> getBuckets();
    
    /**
     * Returns a {@link ValueTuple} for the given {@link ResourceId}.
     */
    public Resource get(ResourceId resource);
    
    /**
     * Returns all {@link ValueTuple}s.
     */
    public Iterable<ResourceId> values();
    
    /**
     * Retruns all {@link ValueTuple}s in the given bucket.
     */
    public Iterable<ResourceId> values(KUID bucketId);
    
    /**
     * Returns all {@link ValueTuple}s that are close to the lookup 
     * {@link KUID} but not any further than the last {@link KUID}.
     */
    public Iterable<ResourceId> values(KUID lookupId, KUID lastId);
    
    /**
     * Returns the size of the {@link Database}.
     */
    public int size();
    
    /**
     * Returns {@code true} if the {@link Database} is empty.
     */
    public boolean isEmpty();
}