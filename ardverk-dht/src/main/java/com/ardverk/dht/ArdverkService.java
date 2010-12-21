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

import java.net.InetAddress;
import java.net.SocketAddress;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.GetConfig;
import com.ardverk.dht.config.LookupConfig;
import com.ardverk.dht.config.PingConfig;
import com.ardverk.dht.config.PutConfig;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.PutEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.routing.Contact;

/**
 * The {@link ArdverkService} defines the basic operations of a DHT.
 */
public interface ArdverkService {

    /**
     * Sends a PING to the given host.
     */
    public ArdverkFuture<PingEntity> ping(
            String host, int port, PingConfig config);
    
    /**
     * Sends a PING to the given host.
     */
    public ArdverkFuture<PingEntity> ping(
            InetAddress address, int port, PingConfig config);
    
    /**
     * Sends a PING to the given host.
     */
    public ArdverkFuture<PingEntity> ping(
            SocketAddress address, PingConfig config);
    
    /**
     * Sends a PING to the given host.
     */
    public ArdverkFuture<PingEntity> ping(
            Contact dst, PingConfig config);
    
    /**
     * Performs a FIND_NODE lookup in the DHT.
     */
    public ArdverkFuture<NodeEntity> lookup(
            KUID lookupId, LookupConfig config);
    
    /**
     * Retrieves a key-value from the DHT.
     */
    public ArdverkFuture<ValueEntity> get(
            KUID valueId, GetConfig config);
    
    /**
     * Stores the given key-value in the DHT.
     */
    public ArdverkFuture<PutEntity> put(
            KUID valueId, byte[] value, PutConfig config);
    
    /**
     * Removes the given {@link KUID} from the DHT.
     */
    public ArdverkFuture<PutEntity> remove(KUID valueId, PutConfig config);
}