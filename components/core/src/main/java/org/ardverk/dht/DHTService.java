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

package org.ardverk.dht;

import java.net.InetAddress;
import java.net.SocketAddress;

import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.config.GetConfig;
import org.ardverk.dht.config.LookupConfig;
import org.ardverk.dht.config.PingConfig;
import org.ardverk.dht.config.PutConfig;
import org.ardverk.dht.entity.NodeEntity;
import org.ardverk.dht.entity.PingEntity;
import org.ardverk.dht.entity.PutEntity;
import org.ardverk.dht.entity.ValueEntity;
import org.ardverk.dht.message.MessageType;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.storage.Resource;
import org.ardverk.dht.storage.Value;
import org.ardverk.dht.storage.ValueTuple;
import org.ardverk.version.VectorClock;


/**
 * The {@link DHTService} defines the basic operations of a DHT.
 */
interface DHTService {

    /**
     * Sends a {@link MessageType#PING} to the given host.
     */
    public DHTFuture<PingEntity> ping(
            String host, int port, PingConfig config);
    
    /**
     * Sends a {@link MessageType#PING} to the given host.
     */
    public DHTFuture<PingEntity> ping(
            InetAddress address, int port, PingConfig config);
    
    /**
     * Sends a {@link MessageType#PING} to the given host.
     */
    public DHTFuture<PingEntity> ping(
            SocketAddress address, PingConfig config);
    
    /**
     * Sends a {@link MessageType#PING} to the given host.
     */
    public DHTFuture<PingEntity> ping(
            Contact dst, PingConfig config);
    
    /**
     * Performs a {@link MessageType#FIND_NODE} lookup in the DHT.
     */
    public DHTFuture<NodeEntity> lookup(
            KUID lookupId, LookupConfig config);
    
    /**
     * Performs a {@link MessageType#FIND_VALUE} lookup in the DHT.
     */
    public DHTFuture<ValueEntity> get(
            Resource resource, GetConfig config);
    
    /**
     * Performs a {@link MessageType#FIND_NODE} lookup followed by 
     * a {@link MessageType#STORE} operation.
     */
    public DHTFuture<PutEntity> put(Resource resource, Value value, 
            VectorClock<KUID> clock, PutConfig config);
    
    /**
     * Removes the given {@link Resource} from the DHT.
     * 
     * <p>NOTE: It's essentially a {@link #put(Resource, Value, PutConfig)} 
     * operation with an empty {@link Value}.
     */
    public DHTFuture<PutEntity> remove(Resource resource, 
            VectorClock<KUID> clock, PutConfig config);
    
    /**
     * Updates the given {@link ValueTuple} with the new {@link Value}.
     */
    public DHTFuture<PutEntity> update(ValueTuple tuple, 
            Value value, PutConfig config);
    
    /**
     * Removes the given {@link ValueTuple} from the DHT.
     */
    public DHTFuture<PutEntity> remove(ValueTuple tuple, PutConfig config);
}