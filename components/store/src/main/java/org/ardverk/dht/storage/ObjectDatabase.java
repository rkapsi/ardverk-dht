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
import org.ardverk.version.VectorClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ObjectDatabase extends AbstractDatabase {
    
    private static final Logger LOG 
        = LoggerFactory.getLogger(ObjectDatabase.class);
    
    private final Trie<KUID, Bucket> database 
        = new PatriciaTrie<KUID, Bucket>();
    
    private final DatabaseConfig config;
    
    public ObjectDatabase() {
        this(new DefaultDatabaseConfig());
    }
    
    public ObjectDatabase(DatabaseConfig config) {
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
        
        ObjectValue ov = null;
        try {
            ov = DefaultObjectValue.valueOf(value);
        } catch (IOException err) {
            LOG.error("IOException", err);
        }
        
        if (!(ov instanceof ObjectValue)) {
            return Status.FAILURE;
        }
        
        return store(key, (ObjectValue)ov);
    }
    
    private synchronized Value store(Key key, ObjectValue value) {
        put(key, value);
        return Status.SUCCESS;
    }
    
    public synchronized Set<KUID> getBuckets() {
        return new HashSet<KUID>(database.keySet());
    }

    @Override
    public synchronized Value get(Key key) {
        /*URI uri = key.getURI();
        String query = uri.getQuery();
        if (query != null) {
            Bucket bucket = database.get(key.getId());
            if (bucket != null) {
                return new KeyList(bucket.keySet());
            }
            return null;
        }*/
        
        return getValue(key);
    }
    
    private synchronized ObjectValue getValue(Key key) {
        Bucket bucket = database.get(key.getId());
        if (bucket == null) {
            return null;
        }
        
        VectorClockMap<?, ObjectValue> map = bucket.get(key);
        if (map == null) {
            return null;
        }
        
        return map.value();
    }
    
    private synchronized void put(Key key, ObjectValue value) {
        KUID bucketId = key.getId();
        
        Bucket bucket = database.get(bucketId);
        if (bucket == null) {
            bucket = new Bucket(bucketId);
            database.put(bucketId, bucket);
        }
        
        VectorClockMap<KUID, ObjectValue> map = bucket.get(key);
        if (map == null) {
            map = new VectorClockMap<KUID, ObjectValue>();
            bucket.put(key, map);
        }
        
        VectorClock<KUID> clock = DefaultObjectValue.getVectorClock(value);
        map.upsert(clock, value);
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
    
    private static class Bucket extends HashMap<Key, VectorClockMap<KUID, ObjectValue>> 
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