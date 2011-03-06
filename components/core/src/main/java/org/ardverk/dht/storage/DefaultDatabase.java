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
import java.util.List;
import java.util.Map.Entry;

import org.ardverk.collection.Cursor;
import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.Trie;
import org.ardverk.dht.KUID;
import org.ardverk.version.Occured;
import org.ardverk.version.VectorClock;


public class DefaultDatabase extends AbstractDatabase {
    
    private final Trie<KUID, ValueTuple> database 
        = new PatriciaTrie<KUID, ValueTuple>();
    
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
        
        Value value = tuple.getValue();
        if (value.isEmpty()) {
            remove(tuple.getId());
            return DefaultCondition.SUCCESS;
        }
        
        Descriptor descriptor = tuple.getDescriptor();
        Resource resource = descriptor.getResource();        
        
        VectorClock<KUID> clock = tuple.getVectorClock();
        VectorClock<KUID> existingClock = null;
        
        ValueTuple existing = get(resource);
        if (existing != null) {
            existingClock = existing.getVectorClock();
        }
        
        if (existingClock == null || existingClock.isEmpty() 
                || clock == null || clock.isEmpty()) {
            add(tuple);
            return DefaultCondition.SUCCESS;
        }
        
        Occured occured = clock.compareTo(existingClock);
        System.out.println(occured);
        if (occured == Occured.AFTER) {
            add(tuple);
            return DefaultCondition.SUCCESS;
        }
        
        return DefaultCondition.FAILURE;
    }
    
    @Override
    public synchronized ValueTuple get(Resource resource) {
        KUID valueId = resource.getId();
        return database.get(valueId);
    }
    
    /**
     * Adds the given {@link ValueTuple}.
     */
    public synchronized ValueTuple add(ValueTuple tuple) {
        assert (!tuple.getValue().isEmpty());
        KUID valueId = tuple.getId();
        return database.put(valueId, tuple);
    }
    
    /**
     * Removes and returns a {@link ValueTuple}.
     */
    public synchronized ValueTuple remove(ValueTuple tuple) {
        return database.remove(tuple.getId());
    }
    
    /**
     * Removes and returns a {@link ValueTuple}.
     */
    public synchronized ValueTuple remove(KUID valueId) {
        return database.remove(valueId);
    }
    
    @Override
    public synchronized int size() {
        return database.size();
    }
    
    @Override
    public synchronized Iterable<ValueTuple> values() {
        return new ArrayList<ValueTuple>(database.values());
    }

    @Override
    public synchronized Iterable<ValueTuple> values(
            final KUID lookupId, final KUID lastId) {
        
        final List<ValueTuple> values = new ArrayList<ValueTuple>();
        database.select(lookupId, new Cursor<KUID, ValueTuple>() {
            @Override
            public Decision select(Entry<? extends KUID, 
                    ? extends ValueTuple> entry) {
                KUID valueId = entry.getKey();
                if (lookupId.isCloserTo(valueId, lastId)) {
                    values.add(entry.getValue());
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
        
        for (ValueTuple tuple : database.values()) {
            buffer.append(tuple).append("\n");
        }
        
        return buffer.toString();
    }
}