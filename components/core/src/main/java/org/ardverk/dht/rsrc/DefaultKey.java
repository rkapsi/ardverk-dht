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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.security.MessageDigest;
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
        
        String keyPath = KeyUtils.getKeyPath(host, port, path);
        
        // Remove all leading and trailing slashes and then split
        String[] tokens = PATTERN.split(
                StringUtils.trim(keyPath, '/'));
        
        if (tokens.length == 0) {
            throw new IllegalArgumentException(uri.toString());
        }
        
        // The first token is the Bucket!
        String bucket = normalize(tokens[0]);
        
        StringBuilder sb = new StringBuilder(
                scheme.length() + keyPath.length() + 3);
        
        sb.append(scheme).append("://");
        
        if (StringUtils.isEmpty(host)) {
            sb.append('/');
        }
        
        sb.append(bucket);
        
        for (int i = 1; i < tokens.length; i++) {
            sb.append('/').append(normalize(tokens[i]));
        }
        
        return new DefaultKey(create(bucket), URI.create(sb.toString()));
    }
    
    private static String normalize(String value) {
        return StringUtils.trim(decode(value), '.');
    }
    
    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StringUtils.UTF_8);
        } catch (UnsupportedEncodingException err) {
            throw new IllegalArgumentException(
                    "UnsupportedEncodingException", err);
        }
    }
    
    private final KUID bucketId;
    
    private final URI uri;
    
    private DefaultKey(KUID bucketId, URI uri) {
        this.bucketId = bucketId;
        this.uri = uri;
    }
    
    @Override
    public Key strip() {
        String query = uri.getQuery();
        if (query == null) {
            return this;
        }
        
        String scheme = uri.getScheme();
        String path = KeyUtils.getKeyPath(uri);
        
        URI normalized = URI.create(scheme + "://" + path);
        return new DefaultKey(bucketId, normalized);
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