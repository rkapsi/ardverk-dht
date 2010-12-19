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
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.PingConfig;
import com.ardverk.dht.entity.PingEntity;

public interface RouteTable extends IoErrorCallback {
    
    /**
     * Returns the {@link RouteTable}'s K parameter as 
     * defined in the Kademlia specification.
     */
    public int getK();
    
    /**
     * Binds the {@link RouteTable} to the given {@link ContactPinger}.
     */
    public void bind(ContactPinger pinger);
    
    /**
     * Unbinds the {@link RouteTable}.
     */
    public void unbind();
    
    /**
     * Returns true if the {@link RouteTable} is bound to a {@link ContactPinger}.
     */
    public boolean isBound();
    
    /**
     * Returns the localhost {@link IContact}.
     */
    public IContact getLocalhost();
    
    /**
     * Adds the given {@link IContact} to the {@link RouteTable}.
     */
    public void add(IContact contact);
    
    /**
     * Returns a {@link IContact} for the given {@link KUID}.
     */
    public IContact get(KUID contactId);
    
    /**
     * See {@link #select(KUID, int)}
     */
    public IContact[] select(KUID contactId);
    
    /**
     * Returns up to <tt>count</tt> number of {@link IContact}s that are
     * XOR bit-wise closest to the given {@link KUID}. The {@link IContact}s
     * array is ordered by closeness.
     */
    public IContact[] select(KUID contactId, int count);
    
    /**
     * Returns all {@link Bucket}s.
     */
    public Bucket[] getBuckets();
    
    /**
     * Returns the number of active {@link IContact}s in the {@link RouteTable}.
     */
    public int size();
    
    /**
     * The {@link #prune()} operation removes all dead {@link IContact}s from
     * the {@link RouteTable} and rebuilds it from the ground up.
     */
    public void prune();
    
    /**
     * Adds the given {@link RouteTableListener}.
     */
    public void addRouteTableListener(RouteTableListener l);
    
    /**
     * Removes the given {@link RouteTableListener}.
     */
    public void removeRouteTableListener(RouteTableListener l);
    
    /**
     * Returns all {@link RouteTableListener}s.
     */
    public RouteTableListener[] getRouteTableListeners();
    
    /**
     * A callback interface the {@link RouteTable} uses to send 
     * ping requests to {@link IContact}s.
     */
    public static interface ContactPinger {

        /**
         * Sends a ping to the given {@link IContact}.
         */
        public ArdverkFuture<PingEntity> ping(IContact contact, PingConfig config);
    }
}