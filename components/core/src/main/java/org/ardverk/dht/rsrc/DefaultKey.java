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

import java.net.URI;
import java.security.MessageDigest;

import org.ardverk.dht.KUID;
import org.ardverk.security.MessageDigestUtils;
import org.ardverk.utils.StringUtils;

public class DefaultKey extends AbstractKey {

    public static String SCHEME = "ardverk";
    
    public static DefaultKey valueOf(String uri) {
        return valueOf(URI.create(uri));
    }
    
    public static DefaultKey valueOf(URI uri) {
        String scheme = uri.getScheme();
        if (!scheme.equals(SCHEME)) {
            throw new IllegalArgumentException(uri.toString());
        }
        
        String path = KeyUtils.getKeyPath(uri);
        
        int p = path.indexOf('/');
        if (p != 0) {
            throw new IllegalArgumentException(uri.toString());
        }
        
        int q = path.indexOf('/', p + 1);
        
        String bucket = path;
        if (q != -1) {
            bucket = path.substring(p, q);
        }
        
        return new DefaultKey(create(bucket), uri);
    }
    
    private final KUID bucketId;
    
    private final URI uri;
    
    private DefaultKey(KUID bucketId, URI uri) {
        this.bucketId = bucketId;
        this.uri = uri;
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