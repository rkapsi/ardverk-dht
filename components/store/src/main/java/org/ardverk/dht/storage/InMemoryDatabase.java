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

import java.io.IOException;
import java.net.URI;
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
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.version.Occured;
import org.ardverk.version.VectorClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InMemoryDatabase extends AbstractDatabase {
    
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDatabase.class);
    
    private final Trie<KUID, Bucket> database 
        = new PatriciaTrie<KUID, Bucket>();
    
    private final DatabaseConfig config;
    
    public InMemoryDatabase() {
        this(new DefaultDatabaseConfig());
    }
    
    public InMemoryDatabase(DatabaseConfig config) {
        this.config = config;
    }
    
    @Override
    public DatabaseConfig getDatabaseConfig() {
        return config;
    }
    
    @Override
    public synchronized Value store(Key key, Value value) {
        if (!isInBucket(key)) {
            return Status.FAILURE;
        }
        
        SimpleValue simpleValue = null;
        try {
            simpleValue = SimpleValue.valueOf(value);
        } catch (IOException err) {
            LOG.error("IOException", err);
        }
        
        if (!(simpleValue instanceof BlobValue)) {
            return Status.FAILURE;
        }
        
        return store(key, (BlobValue)simpleValue);
    }
    
    private synchronized Value store(Key key, BlobValue value) {
        BlobValue existing = getValue(key);
        
        Occured occured = compare(existing, value);
        if (occured == Occured.AFTER) {
            if (value.isEmpty()) {
                remove(key);
            } else {
                put(key, value);
            }
            return Status.SUCCESS;
        }
        
        return Status.conflict(existing);
    }
    
    public synchronized Set<KUID> getBuckets() {
        return new HashSet<KUID>(database.keySet());
    }

    @Override
    public synchronized Value get(Key key) {
        URI uri = key.getURI();
        String query = uri.getQuery();
        if (query != null) {
            Bucket bucket = database.get(key.getId());
            if (bucket != null) {
                return new KeyList(bucket.keySet());
            }
            return null;
        }
        
        return getValue(key);
    }
    
    private synchronized BlobValue getValue(Key key) {
        Bucket bucket = database.get(key.getId());
        return bucket != null ? bucket.get(key) : null;
    }
    
    private synchronized BlobValue put(Key key, BlobValue value) {
        KUID bucketId = key.getId();
        
        Bucket bucket = database.get(bucketId);
        if (bucket == null) {
            bucket = new Bucket(bucketId);
            database.put(bucketId, bucket);
        }
        
        return bucket.put(key, value);
    }
    
    private synchronized BlobValue remove(Key key) {
        KUID bucketId = key.getId();
        
        Bucket bucket = database.get(bucketId);
        if (bucket != null) {
            BlobValue removed = bucket.remove(key);
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
    public synchronized Iterable<Key> keys() {
        List<Key> values = new ArrayList<Key>();
        for (Bucket bucket : database.values()) {
            values.addAll(bucket.keySet());
        }
        return values;
    }
    
    public synchronized Iterable<Key> keys(KUID bucketId) {
        Bucket bucket = database.get(bucketId);
        if (bucket != null) {
            return new ArrayList<Key>(bucket.keySet());
        }
        return Collections.emptyList();
    }

    @Override
    public synchronized Iterable<Key> keys(
            final KUID lookupId, final KUID lastId) {
        
        final List<Key> values = new ArrayList<Key>();
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
    
    private static Occured compare(BlobValue existing, BlobValue value) {
        if (existing == null) {
            return Occured.AFTER;
        }
        
        VectorClock<KUID> clock1 = existing.getVectorClock();
        VectorClock<KUID> clock2 = value.getVectorClock();
        
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
    
    private static class Bucket extends HashMap<Key, BlobValue> 
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