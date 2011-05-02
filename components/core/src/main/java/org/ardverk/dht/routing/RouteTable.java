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

package org.ardverk.dht.routing;

import org.ardverk.dht.KUID;
import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.config.PingConfig;
import org.ardverk.dht.entity.PingEntity;
import org.ardverk.dht.routing.RouteTable.ContactPinger;
import org.ardverk.io.Bindable;

public interface RouteTable extends Bindable<ContactPinger>, IoErrorCallback {
    
    /**
     * Returns the {@link RouteTable}'s K parameter as 
     * defined in the Kademlia specification.
     */
    public int getK();
    
    /**
     * Returns the localhost {@link Localhost}.
     */
    public Localhost getLocalhost();
    
    /**
     * Adds the given {@link Contact} to the {@link RouteTable}.
     */
    public void add(Contact contact);
    
    /**
     * Returns a {@link Contact} for the given {@link KUID}.
     */
    public Contact get(KUID contactId);
    
    /**
     * See {@link #select(KUID, int)}
     */
    public Contact[] select(KUID contactId);
    
    /**
     * Returns up to <tt>count</tt> number of {@link Contact}s that are
     * XOR bit-wise closest to the given {@link KUID}. The {@link Contact}s
     * array is ordered by closeness.
     */
    public Contact[] select(KUID contactId, int count);
    
    /**
     * Returns all {@link Bucket}s.
     */
    public Bucket[] getBuckets();
    
    /**
     * Returns the number of active {@link Contact}s in the {@link RouteTable}.
     */
    public int size();
    
    /**
     * The {@link #prune()} operation removes all dead {@link Contact}s from
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
     * ping requests to {@link Contact}s.
     */
    public static interface ContactPinger {

        /**
         * Sends a ping to the given {@link Contact}.
         */
        public DHTFuture<PingEntity> ping(Contact contact, PingConfig config);
    }
}