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

package com.ardverk.dht;

import org.ardverk.io.Bindable;

import com.ardverk.dht.concurrent.ArdverkFutureService;
import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.Localhost;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;

/**
 * The default interface for a DHT.
 */
public interface DHT extends ArdverkService, BootstrapService, 
        QuickenService, SyncService, ArdverkFutureService, 
        Bindable<Transport> {
    
    /**
     * Returns the localhost {@link Contact}.
     * 
     * @see RouteTable#getLocalhost()
     */
    public Localhost getLocalhost();
    
    /**
     * Returns the {@link DHT}'s {@link RouteTable}.
     */
    public RouteTable getRouteTable();
    
    /**
     * Returns the {@link DHT}'s {@link Database}.
     */
    public Database getDatabase();
}