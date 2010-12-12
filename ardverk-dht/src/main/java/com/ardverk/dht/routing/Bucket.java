/*
 * Copyright 2009-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ardverk.dht.routing;

import com.ardverk.dht.KUID;
import com.ardverk.dht.lang.Identifier;

/**
 * 
 */
public interface Bucket extends Identifier, Longevity {
    
    /**
     * Returns the {@link Bucket}'s depth in the {@link RouteTable} tree.
     */
    public int getDepth();
    
    /**
     * Returns the number of active {@link Contact}s in the {@link Bucket}.
     */
    public int getActiveCount();
    
    /**
     * Returns true if there are no active {@link Contact}s 
     * in the {@link Bucket}.
     */
    public boolean isActiveEmpty();
    
    /**
     * Returns the number of cached {@link Contact}s in the {@link Bucket}.
     */
    public int getCachedCount();
    
    /**
     * Returns true if there are no cached {@link Contact}s 
     * in the {@link Bucket}.
     */
    public boolean isCacheEmpty();
    
    /**
     * Returns all active {@link ContactEntry}ies.
     */
    public ContactEntry[] getActive();
    
    /**
     * Returns all cached {@link ContactEntry}ies.
     */
    public ContactEntry[] getCached();
    
    /**
     * Returns a {@link ContactEntry} for the given {@link KUID}.
     */
    public ContactEntry get(KUID contactId);
    
    /**
     * Returns an active {@link ContactEntry} for the given {@link KUID}.
     */
    public ContactEntry getActive(KUID contactId);
    
    /**
     * Returns a cached {@link ContactEntry} for the given {@link KUID}.
     */
    public ContactEntry getCached(KUID contactId);
    
    /**
     * Returns true if the {@link Bucket} contains a 
     * {@link Contact} with the given {@link KUID}.
     */
    public boolean contains(KUID contactId);
    
    /**
     * Returns true if the {@link Bucket} contains an active 
     * {@link Contact} with the given {@link KUID}.
     */
    public boolean containsActive(KUID contactId);
    
    /**
     * Returns true if the {@link Bucket} contains a cached 
     * {@link Contact} with the given {@link KUID}.
     */
    public boolean containsCached(KUID contactId);
}