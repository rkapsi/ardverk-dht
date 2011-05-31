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
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.ardverk.coding.CodingUtils;
import org.ardverk.collection.CollectionUtils;
import org.ardverk.collection.Cursor;
import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.Trie;
import org.ardverk.dht.KUID;
import org.ardverk.dht.lang.Identifier;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.ByteArrayValue;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.io.IoUtils;
import org.ardverk.io.StreamUtils;
import org.ardverk.security.MessageDigestUtils;
import org.ardverk.utils.ArrayUtils;
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
    public Value store(Contact src, Key key, Value value) {
        if (!isInBucket(key)) {
            return ResponseValue.INTERNAL_SERVER_ERROR;
        }
        
        InputStream in = null;
        try {
            in = value.getContent();
            Context context = Context.valueOf(in);
            return store(key, context, in);
        } catch (Exception err) {
            LOG.error("Exception", err);
            return ResponseValue.INTERNAL_SERVER_ERROR;
        } finally {
            IoUtils.close(in);
        }
    }
    
    private Value store(Key key, Context context, InputStream in) throws IOException {
        
        VectorClock<KUID> vclock = VclockUtils.valueOf(context);
        
        long length = context.getContentLength();
        byte[] data = new byte[(int)Math.max(0L, length)];
        
        MessageDigest md = MessageDigestUtils.createMD5();
        DigestInputStream dis = new DigestInputStream(in, md);
        StreamUtils.readFully(dis, data);
        
        byte[] digest = md.digest();
        
        Header[] contentMD5s = context.getHeaders(Constants.CONTENT_MD5);
        
        if (!ArrayUtils.isEmpty(contentMD5s)) {
            byte[] decoded = Base64.decodeBase64(contentMD5s[0].getValue());
            if (!Arrays.equals(decoded, digest)) {
                return ResponseValue.INTERNAL_SERVER_ERROR;
            }
            
            // Remove the Content-MD5s and replace it/them with an ETag!
            context.removeHeaders(contentMD5s);   
        }
        
        String etag = "\"" + CodingUtils.encodeBase16(digest) + "\"";
        context.setHeader(Constants.ETAG, etag);
        
        put(key, vclock, new ContextValue(context, new ByteArrayValue(data)));
        
        return ResponseValue.createOk(vclock);
    }
    
    private synchronized void put(Key key, VectorClock<KUID> vclock, ContextValue value) {
        KUID bucketId = key.getId();
        
        Bucket bucket = database.get(bucketId);
        if (bucket == null) {
            bucket = new Bucket(bucketId);
            database.put(bucketId, bucket);
        }
        
        VclockMap<KUID, ContextValue> map = bucket.get(key);
        if (map == null) {
            map = new VclockMap<KUID, ContextValue>();
            bucket.put(key, map);
        }
        
        value.setHeader(Constants.VCLOCK, 
                VclockUtils.toString(vclock));
        
        map.upsert(vclock, value);
    }
    
    public synchronized Set<KUID> getBuckets() {
        return new HashSet<KUID>(database.keySet());
    }

    @Override
    public Value get(Key key) {
        /*URI uri = key.getURI();
        String query = uri.getQuery();
        if (query != null) {
            Bucket bucket = database.get(key.getId());
            if (bucket != null) {
                return new KeyList(bucket.keySet());
            }
            return null;
        }*/
        
        ContextValue[] values = getValues(key);
        if (ArrayUtils.isEmpty(values)) {
            return ResponseValue.NOT_FOUND;
        }
        
        if (values.length == 1) {
            return CollectionUtils.first(values);
        }
        
        return ResponseValue.MULTIPLE_CHOICES;
    }
    
    private synchronized ContextValue[] getValues(Key key) {
        Bucket bucket = database.get(key.getId());
        if (bucket == null) {
            return null;
        }
        
        VclockMap<?, ContextValue> map = bucket.get(key);
        if (map == null) {
            return null;
        }
        
        return map.values(ContextValue.class);
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
    
    private static class Bucket extends HashMap<Key, VclockMap<KUID, ContextValue>> 
            implements Identifier {
        
        private static final long serialVersionUID = -8794611016380746313L;
        
        private final Properties properties = new ObjectProperties();
        
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