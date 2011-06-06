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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.protocol.HTTP;
import org.ardverk.coding.CodingUtils;
import org.ardverk.collection.CollectionUtils;
import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.Trie;
import org.ardverk.dht.KUID;
import org.ardverk.dht.lang.Identifier;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.KeyUtils;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.io.IoUtils;
import org.ardverk.io.StreamUtils;
import org.ardverk.security.MessageDigestUtils;
import org.ardverk.utils.ArrayUtils;
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
    public Response store(Contact src, Key key, Value value) {
        
        InputStream in = null;
        try {
            in = value.getContent();
            
            Request request = Request.valueOf(in);
            
            Response response = store(src, key, request, in);
            if (response == null) {
                response = ResponseFactory.createNotFound();
            }
            return response;
            
        } catch (Exception err) {
            LOG.error("Exception", err);
            return Response.INTERNAL_SERVER_ERROR;
        } finally {
            IoUtils.close(in);
        }
    }
    
    private Response store(Contact src, Key key, 
            Request request, InputStream in) throws IOException {
        
        Method method = request.getMethod();
        switch (method) {
            case GET:
                return get(src, key);
            case PUT:
                break;
            default:
                throw new IOException(method.toString());
        }
        
        Context context = request.getContext();
        
        long length = context.getContentLength();
        byte[] data = new byte[(int)Math.max(0L, length)];
        
        MessageDigest md = MessageDigestUtils.createMD5();
        DigestInputStream dis = new DigestInputStream(in, md);
        StreamUtils.readFully(dis, data);
        
        byte[] digest = md.digest();
        
        String contentMD5 = context.getContentMD5();
        if (contentMD5 != null) {
            byte[] decoded = Base64.decodeBase64(contentMD5);
            if (!Arrays.equals(decoded, digest)) {
                return Response.INTERNAL_SERVER_ERROR;
            }  
        } else {
            context.addHeader(Constants.CONTENT_MD5, 
                    Base64.encodeBase64String(digest));
        }
        
        String etag = "\"" + CodingUtils.encodeBase16(digest) + "\"";
        context.addHeader(Constants.ETAG, etag);
        
        String contentType = context.getStringValue(
                HTTP.CONTENT_TYPE, HTTP.OCTET_STREAM_TYPE);
        
        ValueEntity entity = new ByteArrayValueEntity(contentType, data);
        
        Header[] response = put(key, context, entity);
        
        return ResponseFactory.createOk(response);
    }
    
    private synchronized Header[] put(Key key, 
            Context context, ValueEntity entity) throws IOException {
        
        KUID bucketId = key.getId();
        
        Bucket bucket = database.get(bucketId);
        if (bucket == null) {
            bucket = new Bucket(bucketId);
            database.put(bucketId, bucket);
        }
        
        Key normalized = key.normalize();
        VclockMap map = bucket.get(normalized);
        if (map == null) {
            map = new VclockMap();
            bucket.put(normalized, map);
        }
        
        Vclock vclock = VclockUtils.valueOf(context);
        
        Header header = context.addHeader(Constants.VCLOCK, 
                vclock.toString());
        
        Header vtag = context.addHeader(Constants.VTAG, 
                vclock.getVTag());
        
        map.upsert(vclock, context, entity);
        
        return new Header[] { header, vtag };
    }
    
    public synchronized Set<KUID> getBuckets() {
        return new HashSet<KUID>(database.keySet());
    }

    @Override
    public Response get(Contact src, Key key) {
        String vtag = null;
        Map<String, String> query = KeyUtils.getQueryString(key);
        if (!query.isEmpty()) {
            if (query.containsKey("list")) {
                return list(key);
            } else if (query.containsKey("vtag")) {
                vtag = query.get("vtag");
            }
        }
        
        VclockMap.Entry[] values = getValues(key, vtag);
        if (ArrayUtils.isEmpty(values)) {
            return ResponseFactory.createNotFound();
        }
        
        if (values.length == 1) {
            VclockMap.Entry entry = CollectionUtils.first(values);
            return ResponseFactory.createOk(entry.getContext(), entry.getValueEntity());
        }
        
        return MultipleChoicesResponse.create(key, values);
    }
    
    private synchronized VclockMap.Entry[] getValues(Key key, String vtag) {
        Bucket bucket = database.get(key.getId());
        if (bucket == null) {
            return null;
        }
        
        VclockMap map = bucket.get(key);
        if (map == null) {
            return null;
        }
        
        if (vtag != null) {
            VclockMap.Entry entry = map.value(vtag);
            if (entry != null) {
                return new VclockMap.Entry[] { entry };
            }
            return null;
        }
        
        return map.values();
    }
    
    private synchronized Response list(Key prefix) {
        Bucket bucket = database.get(prefix.getId());
        if (bucket == null) {
            return null;
        }
        
        String path = prefix.getPath();
        
        List<Key> keys = new ArrayList<Key>();
        for (Key existing : bucket.keySet()) {
            if (existing.getPath().startsWith(path)) {
                keys.add(existing);
            }
        }
        
        return ListBucketResponse.create(prefix, keys);
    }
    
    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        for (Bucket bucket : database.values()) {
            sb.append(bucket).append("\n");
        }
        return sb.toString();
    }
    
    private static class Bucket extends HashMap<Key, VclockMap> 
            implements Identifier {
        
        private static final long serialVersionUID = -8794611016380746313L;
        
        private final Context context = new Context();
        
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