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
import org.ardverk.dht.concurrent.ArdverkFuture;
import org.ardverk.dht.entity.BootstrapEntity;
import org.ardverk.dht.entity.NodeEntity;
import org.ardverk.dht.entity.PingEntity;
import org.ardverk.dht.entity.PutEntity;
import org.ardverk.dht.entity.QuickenEntity;
import org.ardverk.dht.entity.SyncEntity;
import org.ardverk.dht.entity.ValueEntity;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.storage.Value;


/**
 * An interface that describes a simpler to use version of the {@link DHT}.
 */
public interface EasyDHT extends DHT, Closeable {
    
    public ArdverkFuture<PingEntity> ping(String host, int port);

    public ArdverkFuture<PingEntity> ping(InetAddress address, int port);

    public ArdverkFuture<PingEntity> ping(SocketAddress address);
    
    public ArdverkFuture<PingEntity> ping(Contact dst);
    
    public ArdverkFuture<NodeEntity> lookup(KUID lookupId);

    public ArdverkFuture<ValueEntity> get(KUID valueId);

    public ArdverkFuture<PutEntity> put(KUID valueId, Value value);

    public ArdverkFuture<PutEntity> remove(KUID valueId);
    
    public ArdverkFuture<BootstrapEntity> bootstrap(String host, int port);

    public ArdverkFuture<BootstrapEntity> bootstrap(
            InetAddress address, int port);

    public ArdverkFuture<BootstrapEntity> bootstrap(SocketAddress address);
    
    public ArdverkFuture<BootstrapEntity> bootstrap(Contact contact);

    public ArdverkFuture<QuickenEntity> quicken();
    
    public ArdverkFuture<SyncEntity> sync();
}