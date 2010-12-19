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

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.BootstrapEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.PutEntity;
import com.ardverk.dht.entity.QuickenEntity;
import com.ardverk.dht.entity.SyncEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.routing.Contact;

/**
 * An interface that describes a simpler to use {@link DHT}.
 */
public interface SimpleDHT {

    public void bind(int port) throws IOException;
    
    public void bind(String host, int port) throws IOException;
    
    public void bind(InetAddress bindaddr, int port) throws IOException;
    
    public void bind(SocketAddress address) throws IOException;
    
    public ArdverkFuture<PingEntity> ping(String host, int port);

    public ArdverkFuture<PingEntity> ping(InetAddress address, int port);

    public ArdverkFuture<PingEntity> ping(SocketAddress address);
    
    public ArdverkFuture<PingEntity> ping(Contact dst);
    
    public ArdverkFuture<NodeEntity> lookup(KUID lookupId);

    public ArdverkFuture<ValueEntity> get(KUID key);

    public ArdverkFuture<PutEntity> put(KUID key, byte[] value);

    public ArdverkFuture<PutEntity> remove(KUID key);
    
    public ArdverkFuture<BootstrapEntity> bootstrap(String host, int port);

    public ArdverkFuture<BootstrapEntity> bootstrap(
            InetAddress address, int port);

    public ArdverkFuture<BootstrapEntity> bootstrap(SocketAddress address);
    
    public ArdverkFuture<BootstrapEntity> bootstrap(Contact contact);

    public ArdverkFuture<QuickenEntity> quicken();
    
    public ArdverkFuture<SyncEntity> sync();
}
