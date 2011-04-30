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
import java.util.ServiceLoader;

public abstract class KeyFactory {

    private static final ServiceLoader<KeyFactory> FACTORIES 
        = ServiceLoader.load(KeyFactory.class);
    
    public static ResourceId parseKey(String uri) {
        return parseKey(URI.create(uri));
    }
    
    public static ResourceId parseKey(URI uri) {
        for (KeyFactory factory : FACTORIES) {
            ResourceId resourceId = factory.valueOf(uri);
            if (resourceId != null) {
                return resourceId;
            }
        }
        
        throw new IllegalArgumentException(uri.toString());
    }

    /**
     * 
     */
    public abstract ResourceId valueOf(URI uri);
}
