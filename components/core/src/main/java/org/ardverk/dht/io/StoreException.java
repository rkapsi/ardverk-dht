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

package org.ardverk.dht.io;

import java.util.concurrent.TimeUnit;

import org.ardverk.dht.lang.DHTException;
import org.ardverk.dht.message.Content;
import org.ardverk.dht.storage.ResourceId;


/**
 * The {@link StoreException} is thrown if a {@link Content} couldn't
 * be stored in the DHT at all.
 */
public class StoreException extends DHTException {
    
    private static final long serialVersionUID = -1874658787780091708L;

    private final ResourceId resourceId;
    
    private final Content content;
    
    public StoreException(ResourceId resourceId, Content content, 
            long time, TimeUnit unit) {
        super(time, unit);
        
        this.resourceId = resourceId;
        this.content = content;
    }

    /**
     * Returns the {@link ResourceId} that failed to be stored.
     */
    public ResourceId getResourceId() {
        return resourceId;
    }
    
    /**
     * Returns the {@link Content} that failed to be stored.
     */
    public Content getContent() {
        return content;
    }
}