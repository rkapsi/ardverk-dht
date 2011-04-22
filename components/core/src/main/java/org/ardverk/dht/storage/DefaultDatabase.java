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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ardverk.collection.Cursor;
import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.Trie;
import org.ardverk.dht.KUID;
import org.ardverk.version.Occured;
import org.ardverk.version.VectorClock;


public class DefaultDatabase extends AbstractDatabase {
    
    private final Trie<KUID, Map<Resource, ValueTuple>> database 
        = new PatriciaTrie<KUID, Map<Resource, ValueTuple>>();
    
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
    public synchronized Condition store(ValueTuple tuple) {
        
        Descriptor descriptor = tuple.getDescriptor();
        Resource resource = descriptor.getResource();
        
        Value value = tuple.getValue();
        if (value.isEmpty()) {
            remove(resource);
            return DefaultCondition.SUCCESS;
        }
        
        ValueTuple existing = get(resource);
        
        Occured occured = compare(existing, tuple);
        if (occured == Occured.AFTER) {
            add(tuple);
            return DefaultCondition.SUCCESS;
        }
        
        return DefaultCondition.FAILURE;
    }
    
    @Override
    public synchronized ValueTuple get(Resource resource) {
        KUID valueId = resource.getId();
        Map<Resource, ValueTuple> bucket = database.get(valueId);
        return bucket != null ? bucket.get(resource) : null;
    }
    
    /**
     * Adds the given {@link ValueTuple}.
     */
    public synchronized ValueTuple add(ValueTuple tuple) {
        assert (!tuple.getValue().isEmpty());
        
        Descriptor descriptor = tuple.getDescriptor();
        Resource resource = descriptor.getResource();
        KUID valueId = resource.getId();
        
        Map<Resource, ValueTuple> bucket = database.get(valueId);
        if (bucket == null) {
            bucket = new HashMap<Resource, ValueTuple>();
            database.put(valueId, bucket);
        }
        
        return bucket.put(resource, tuple);
    }
    
    /**
     * Removes and returns a {@link ValueTuple}.
     */
    public synchronized ValueTuple remove(Resource resource) {
        KUID valueId = resource.getId();
        
        Map<Resource, ValueTuple> bucket = database.get(valueId);
        if (bucket != null) {
            ValueTuple removed = bucket.remove(resource);
            if (bucket.isEmpty()) {
                database.remove(valueId);
            }
            return removed;
        }
        
        return null;
    }
    
    /**
     * Removes and returns a {@link ValueTuple}.
     */
    public synchronized Map<Resource, ValueTuple> remove(KUID valueId) {
        return database.remove(valueId);
    }
    
    @Override
    public synchronized int size() {
        int size = 0;
        for (Map<?, ?> bucket : database.values()) {
            size += bucket.size();
        }
        return size;
    }
    
    @Override
    public synchronized Iterable<ValueTuple> values() {
        List<ValueTuple> values = new ArrayList<ValueTuple>();
        for (Map<?, ValueTuple> bucket : database.values()) {
            values.addAll(bucket.values());
        }
        return values;
    }

    @Override
    public synchronized Iterable<ValueTuple> values(
            final KUID lookupId, final KUID lastId) {
        
        final List<ValueTuple> values = new ArrayList<ValueTuple>();
        database.select(lookupId, new Cursor<KUID, Map<?, ? extends ValueTuple>>() {
            @Override
            public Decision select(Entry<? extends KUID, 
                    ? extends Map<?, ? extends ValueTuple>> entry) {
                KUID valueId = entry.getKey();
                if (lookupId.isCloserTo(valueId, lastId)) {
                    values.addAll(entry.getValue().values());
                    return Decision.CONTINUE;
                }
                return Decision.EXIT;
            }
        });
        
        return values;
    }
    
    @Override
    public synchronized String toString() {
        StringBuilder buffer = new StringBuilder();
        
        for (Map.Entry<KUID, Map<Resource, ValueTuple>> bucket : database.entrySet()) {
            KUID bucketId = bucket.getKey();
            Map<Resource, ValueTuple> values = bucket.getValue();
            
            buffer.append(bucketId).append("={\n");
            for (Map.Entry<Resource, ValueTuple> entry : values.entrySet()) {
                buffer.append("  ").append(entry);
            }
            buffer.append("}\n");
        }
        
        return buffer.toString();
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
}