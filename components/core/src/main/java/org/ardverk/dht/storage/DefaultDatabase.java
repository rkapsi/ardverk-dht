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
    public synchronized Status store(Resource resource) {
        return store(resource.getResourceId(), 
                ByteArrayValue.create(resource));
    }
    
    private synchronized Status store(ResourceId resourceId, ByteArrayValue value) {
        ByteArrayValue existing = getValue(resourceId);
        
        Occured occured = compare(existing, value);
        if (occured == Occured.AFTER) {
            if (value.isEmpty()) {
                remove(resourceId);
            } else {
                put(resourceId, value);
            }
            return DefaultStatus.SUCCESS;
        }
        
        return DefaultStatus.conflict(existing.toResource(resourceId));
    }
    
    @Override
    public synchronized Set<KUID> getBuckets() {
        return new HashSet<KUID>(database.keySet());
    }

    @Override
    public synchronized Resource get(ResourceId resourceId) {
        ByteArrayValue value = getValue(resourceId);
        return value != null ? value.toResource(resourceId) : null;
    }
    
    private synchronized ByteArrayValue getValue(ResourceId resourceId) {
        Bucket bucket = database.get(resourceId.getId());
        return bucket != null ? bucket.get(resourceId) : null;
    }
    
    private synchronized ByteArrayValue put(ResourceId resourceId, ByteArrayValue value) {
        KUID bucketId = resourceId.getId();
        
        Bucket bucket = database.get(bucketId);
        if (bucket == null) {
            bucket = new Bucket(bucketId);
            database.put(bucketId, bucket);
        }
        
        return bucket.put(resourceId, value);
    }
    
    private synchronized ByteArrayValue remove(ResourceId resourceId) {
        KUID bucketId = resourceId.getId();
        
        Bucket bucket = database.get(bucketId);
        if (bucket != null) {
            ByteArrayValue removed = bucket.remove(resourceId);
            if (bucket.isEmpty()) {
                database.remove(bucketId);
            }
            return removed;
        }
        
        return null;
    }
    
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
    public synchronized Iterable<ResourceId> values() {
        List<ResourceId> values = new ArrayList<ResourceId>();
        for (Bucket bucket : database.values()) {
            values.addAll(bucket.keySet());
        }
        return values;
    }
    
    @Override
    public synchronized Iterable<ResourceId> values(KUID bucketId) {
        Bucket bucket = database.get(bucketId);
        if (bucket != null) {
            return new ArrayList<ResourceId>(bucket.keySet());
        }
        return Collections.emptyList();
    }

    @Override
    public synchronized Iterable<ResourceId> values(
            final KUID lookupId, final KUID lastId) {
        
        final List<ResourceId> values = new ArrayList<ResourceId>();
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
    
    private static Occured compare(ByteArrayValue existing, ByteArrayValue resource) {
        if (existing == null) {
            return Occured.AFTER;
        }
        
        VectorClock<KUID> clock1 = existing.getVectorClock();
        VectorClock<KUID> clock2 = resource.getVectorClock();
        
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
    
    private static class Bucket extends HashMap<ResourceId, ByteArrayValue> 
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
            for (Map.Entry<?, ?> entry : entrySet()) {
                sb.append("  ").append(entry);
            }
            sb.append("}");
            
            return sb.toString();
        }
    }
}