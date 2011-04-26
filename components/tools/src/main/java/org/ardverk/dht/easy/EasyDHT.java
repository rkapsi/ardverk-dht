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

package org.ardverk.dht.easy;

import java.io.Closeable;
import java.net.InetAddress;
import java.net.SocketAddress;

import org.ardverk.dht.DHT;
import org.ardverk.dht.KUID;
import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.entity.BootstrapEntity;
import org.ardverk.dht.entity.NodeEntity;
import org.ardverk.dht.entity.PingEntity;
import org.ardverk.dht.entity.PutEntity;
import org.ardverk.dht.entity.QuickenEntity;
import org.ardverk.dht.entity.SyncEntity;
import org.ardverk.dht.entity.ValueEntity;
import org.ardverk.dht.message.Content;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.storage.ResourceId;


/**
 * An interface that describes a simpler to use version of the {@link DHT}.
 */
public interface EasyDHT extends DHT, Closeable {
    
    public DHTFuture<PingEntity> ping(String host, int port);

    public DHTFuture<PingEntity> ping(InetAddress address, int port);

    public DHTFuture<PingEntity> ping(SocketAddress address);
    
    public DHTFuture<PingEntity> ping(Contact dst);
    
    public DHTFuture<NodeEntity> lookup(KUID lookupId);

    public DHTFuture<ValueEntity> get(ResourceId resourceId);

    public DHTFuture<PutEntity> put(ResourceId resourceId, Content content);

    public DHTFuture<BootstrapEntity> bootstrap(String host, int port);

    public DHTFuture<BootstrapEntity> bootstrap(
            InetAddress address, int port);

    public DHTFuture<BootstrapEntity> bootstrap(SocketAddress address);
    
    public DHTFuture<BootstrapEntity> bootstrap(Contact contact);

    public DHTFuture<QuickenEntity> quicken();
    
    public DHTFuture<SyncEntity> sync();
}