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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ardverk.collection.Cursor;
import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.Trie;
import org.ardverk.dht.KUID;
import org.ardverk.dht.lang.Identifier;
import org.ardverk.version.Occured;
import org.ardverk.version.VectorClock;


public class DefaultDatabase extends AbstractDatabase {
    
    private final Trie<KUID, Bucket> database 
        = new PatriciaTrie<KUID, Bucket>();
    
    private final DatabaseConfig config;
    
    public DefaultDatabase() {
        this(new DefaultDatabaseConfig());
    }
    
    public DefaultDatabase(DatabaseConfig config) {
        this.config = config;
    }
    
    @Override
    public DatabaseConfig getDatabaseConfig() {
        return config;
    }

    @Override
    public synchronized Status store(ValueTuple tuple) {
        
        Descriptor descriptor = tuple.getDescriptor();
        Resource resource = descriptor.getResource();
        
        ValueTuple existing = get(resource);
        
        Occured occured = compare(existing, tuple);
        if (occured == Occured.AFTER) {
            Value value = tuple.getValue();
            if (value.isEmpty()) {
                remove(resource);
            } else {
                add(tuple);
            }
            return DefaultStatus.SUCCESS;
        }
        
        return DefaultStatus.conflict(existing);
    }
    
    @Override
    public synchronized Set<KUID> getBuckets() {
        return new HashSet<KUID>(database.keySet());
    }

    @Override
    public synchronized ValueTuple get(Resource resource) {
        Bucket bucket = database.get(resource.getId());
        return bucket != null ? bucket.get(resource) : null;
    }
    
    /**
     * Adds the given {@link ValueTuple}.
     */
    public synchronized ValueTuple add(ValueTuple tuple) {
        assert (!tuple.getValue().isEmpty());
        
        Descriptor descriptor = tuple.getDescriptor();
        Resource resource = descriptor.getResource();
        KUID bucketId = resource.getId();
        
        Bucket bucket = database.get(bucketId);
        if (bucket == null) {
            bucket = new Bucket(bucketId);
            database.put(bucketId, bucket);
        }
        
        return bucket.put(resource, tuple);
    }
    
    /**
     * Removes and returns a {@link ValueTuple}.
     */
    public synchronized ValueTuple remove(Resource resource) {
        KUID bucketId = resource.getId();
        
        Bucket bucket = database.get(bucketId);
        if (bucket != null) {
            ValueTuple removed = bucket.remove(resource);
            if (bucket.isEmpty()) {
                database.remove(bucketId);
            }
            return removed;
        }
        
        return null;
    }
    
    /**
     * Removes and returns a {@link ValueTuple}.
     */
    public synchronized Bucket remove(KUID bucketId) {
        return database.remove(bucketId);
    }
    
    @Override
    public synchronized int size() {
        int size = 0;
        for (Bucket bucket : database.values()) {
            size += bucket.size();
        }
        return size;
    }
    
    @Override
    public synchronized Iterable<Resource> values() {
        List<Resource> values = new ArrayList<Resource>();
        for (Bucket bucket : database.values()) {
            values.addAll(bucket.keySet());
        }
        return values;
    }
    
    @Override
    public synchronized Iterable<Resource> values(KUID bucketId) {
        Bucket bucket = database.get(bucketId);
        if (bucket != null) {
            return new ArrayList<Resource>(bucket.keySet());
        }
        return Collections.emptyList();
    }

    @Override
    public synchronized Iterable<Resource> values(
            final KUID lookupId, final KUID lastId) {
        
        final List<Resource> values = new ArrayList<Resource>();
        database.select(lookupId, new Cursor<KUID, Bucket>() {
            @Override
            public Decision select(Entry<? extends KUID, 
                    ? extends Bucket> entry) {
                KUID bucketId = entry.getKey();
                if (lookupId.isCloserTo(bucketId, lastId)) {
                    values.addAll(entry.getValue().keySet());
                    return Decision.CONTINUE;
                }
                return Decision.EXIT;
            }
        });
        
        return values;
    }
    
    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        for (Bucket bucket : database.values()) {
            sb.append(bucket).append("\n");
        }
        return sb.toString();
    }
    
    private static Occured compare(ValueTuple existing, ValueTuple tuple) {
        if (existing == null) {
            return Occured.AFTER;
        }
        
        VectorClock<KUID> clock1 = existing.getDescriptor().getVectorClock();
        VectorClock<KUID> clock2 = tuple.getDescriptor().getVectorClock();
        
        return compare(clock1, clock2);
    }
    
    private static Occured compare(VectorClock<KUID> existing, 
            VectorClock<KUID> clock) {
        if (existing == null || existing.isEmpty()
                || clock == null || clock.isEmpty()) {
            return Occured.AFTER;
        }
        
        return clock.compareTo(existing);
    }
    
    private static class Bucket extends HashMap<Resource, ValueTuple> 
            implements Identifier {
        
        private static final long serialVersionUID = -8794611016380746313L;

        private final KUID bucketId;
        
        public Bucket(KUID bucketId) {
            this.bucketId = bucketId;
        }
        
        @Override
        public KUID getId() {
            return bucketId;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            
            sb.append(getId()).append("={\n");
            for (Map.Entry<Resource, ValueTuple> entry : entrySet()) {
                sb.append("  ").append(entry);
            }
            sb.append("}");
            
            return sb.toString();
        }
    }
}