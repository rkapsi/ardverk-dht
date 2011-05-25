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

import java.net.InetAddress;
import java.net.SocketAddress;

import org.ardverk.dht.ArdverkDHT;
import org.ardverk.dht.KUID;
import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.entity.BootstrapEntity;
import org.ardverk.dht.entity.NodeEntity;
import org.ardverk.dht.entity.PingEntity;
import org.ardverk.dht.entity.PutEntity;
import org.ardverk.dht.entity.QuickenEntity;
import org.ardverk.dht.entity.ValueEntity;
import org.ardverk.dht.message.MessageFactory;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.RouteTable;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.dht.storage.Database;


public class DefaultEasyDHT extends ArdverkDHT implements EasyDHT {
        
    private final EasyConfig config;
    
    public DefaultEasyDHT(EasyConfig config, MessageFactory messageFactory, 
            RouteTable routeTable, Database database) {
        super(messageFactory, routeTable, database);
        this.config = config;
    }
    
    @Override
    public DHTFuture<PingEntity> ping(String host, int port) {
        return ping(host, port, config.getPingConfig());
    }

    @Override
    public DHTFuture<PingEntity> ping(InetAddress address, int port) {
        return ping(address, port, config.getPingConfig());
    }

    @Override
    public DHTFuture<PingEntity> ping(SocketAddress address) {
        return ping(address, config.getPingConfig());
    }
    
    @Override
    public DHTFuture<PingEntity> ping(Contact dst) {
        return ping(dst, config.getPingConfig());
    }
    
    @Override
    public DHTFuture<NodeEntity> lookup(KUID lookupId) {
        return lookup(lookupId, config.getLookupConfig());
    }

    @Override
    public DHTFuture<ValueEntity> get(Key key) {
        return get(key, config.getGetConfig());
    }

    @Override
    public DHTFuture<PutEntity> put(Key key, Value content) {
        return put(key, content, config.getPutConfig());
    }
    
    @Override
    public DHTFuture<BootstrapEntity> bootstrap(String host, int port) {
        return bootstrap(host, port, config.getBootstrapConfig());
    }

    @Override
    public DHTFuture<BootstrapEntity> bootstrap(
            InetAddress address, int port) {
        return bootstrap(address, port, config.getBootstrapConfig());
    }

    @Override
    public DHTFuture<BootstrapEntity> bootstrap(SocketAddress address) {
        return bootstrap(address, config.getBootstrapConfig());
    }

    @Override
    public DHTFuture<BootstrapEntity> bootstrap(Contact contact) {
        return bootstrap(contact, config.getBootstrapConfig());
    }

    @Override
    public DHTFuture<QuickenEntity> quicken() {
        return quicken(config.getQuickenConfig());
    }
}