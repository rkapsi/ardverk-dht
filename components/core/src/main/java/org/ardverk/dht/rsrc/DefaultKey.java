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

package org.ardverk.dht.rsrc;

import static org.ardverk.utils.StringUtils.decodeURL;
import static org.ardverk.utils.StringUtils.isEmpty;
import static org.ardverk.utils.StringUtils.trim;

import java.net.URI;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.ardverk.dht.KUID;
import org.ardverk.security.MessageDigestUtils;
import org.ardverk.utils.StringUtils;

public class DefaultKey extends AbstractKey {

    private static final Pattern PATTERN = Pattern.compile("/");
    
    public static String SCHEME = "ardverk";
    
    public static DefaultKey valueOf(String uri) {
        return valueOf(URI.create(uri));
    }
    
    public static DefaultKey valueOf(URI uri) {
        String scheme = uri.getScheme();
        if (!scheme.equals(SCHEME)) {
            throw new IllegalArgumentException(uri.toString());
        }
        
        String host = uri.getHost();
        int port = uri.getPort();
        String path = uri.getPath();
        
        List<String> normalized = normalize(path);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(uri.toString());
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(scheme).append("://");
        
        String bucket = null;
        if (!isEmpty(host)) {
            int p = sb.length();
            sb.append(host);
            if (port != -1) {
                sb.append(':').append(port);
            }
            // host:port
            bucket = sb.substring(p);
        } else {
            // Fist segment of the path
            bucket = normalized.get(0);
        }
        
        for (String element : normalized) {
            sb.append('/').append(element);
        }
        
        String query = uri.getQuery();
        if (!isEmpty(query)) {
            sb.append('?').append(query);
        }
        
        return new DefaultKey(bucket, URI.create(sb.toString()));
    }
    
    private static List<String> normalize(String value) {
        String[] tokens = PATTERN.split(value, '/');
        List<String> dst = new ArrayList<String>(tokens.length);
        
        for (String token : tokens) {
            String normalized = trim(decodeURL(token), '.');
            
            if (!isEmpty(normalized)) {
                dst.add(normalized);
            }
        }
        
        return dst;
    }
    
    private final KUID bucketId;
    
    private final String bucket;
    
    private final URI uri;
    
    private DefaultKey(String bucket, URI uri) {
        this(create(bucket), bucket, uri);
    }
    
    private DefaultKey(KUID bucketId, String bucket, URI uri) {
        this.bucketId = bucketId;
        this.bucket = bucket;
        this.uri = uri;
    }
    
    @Override
    public String getBucket() {
        return bucket;
    }

    @Override
    public Key strip() {
        String query = uri.getQuery();
        if (query == null) {
            return this;
        }
        
        String scheme = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();
        String path = uri.getPath();
        
        StringBuilder sb = new StringBuilder();
        sb.append(scheme).append("://");
        
        if (!isEmpty(host)) {
            sb.append(host);
            if (port != -1) {
                sb.append(':').append(port);
            }
        }
        
        sb.append(path);
        
        return new DefaultKey(bucketId, bucket,
                URI.create(sb.toString()));
    }

    @Override
    public KUID getId() {
        return bucketId;
    }
    
    @Override
    public URI getURI() {
        return uri;
    }
    
    private static byte[] digest(String bucket) {
        MessageDigest md = MessageDigestUtils.createSHA1();
        return md.digest(StringUtils.getBytes(bucket));
    }
    
    private static KUID create(String bucket) {
        return KUID.create(digest(bucket));
    }
}