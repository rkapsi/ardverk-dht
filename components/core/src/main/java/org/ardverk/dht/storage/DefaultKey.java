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

import java.net.URI;

import org.ardverk.dht.KUID;

public class DefaultKey extends AbstractKey {

    private static final KUID BUCKET = KUID.createRandom(20);
    
    private final KUID valueId;

    private final URI uri;
    
    private DefaultKey(KUID valueId, URI uri) {
        this.valueId = valueId;
        this.uri = uri;
    }
    
    @Override
    public KUID getId() {
        return valueId;
    }

    @Override
    public URI getURI() {
        return uri;
    }
    
    public static Key valueOf(URI uri) {
        return new DefaultKey(parse(uri), uri);
    }
    
    public static Key valueOf(KUID valueId) {
        return valueOf(BUCKET, valueId);
    }
    
    public static Key valueOf(KUID bucketId, KUID valueId) {
        return new DefaultKey(bucketId, create(bucketId, valueId, null));
    }
    
    public static Key valueOf(String query) {
        return valueOf(BUCKET, query);
    }
    
    public static Key valueOf(KUID bucketId, String query) {
        return new DefaultKey(bucketId, create(bucketId, null, query));
    }
    
    private static URI create(KUID bucketId, KUID valueId, String query) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("ardverk:///").append(bucketId.toHexString());
        
        if (valueId != null) {
            sb.append("/").append(valueId.toHexString());
        }
        
        if (query != null && !query.isEmpty()) {
            sb.append("?").append(query);
        }
        
        return URI.create(sb.toString());
    }
    
    private static KUID parse(URI uri) {
        String scheme = uri.getScheme();
        if (!scheme.equals("ardverk")) {
            throw new IllegalArgumentException();
        }
        
        String path = uri.getPath();
        while (!path.isEmpty() && path.startsWith("/")) {
            path = path.substring(1);
        }
        
        int p = path.indexOf("/");
        if (p != -1) {
            path = path.substring(0, p);
        }
        
        return KUID.create(path, 16);
    }
}