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

package org.ardverk.dht.easy;

import java.net.InetAddress;
import java.net.SocketAddress;

import org.ardverk.dht.ArdverkDHT;
import org.ardverk.dht.KUID;
import org.ardverk.dht.concurrent.ArdverkFuture;
import org.ardverk.dht.entity.BootstrapEntity;
import org.ardverk.dht.entity.NodeEntity;
import org.ardverk.dht.entity.PingEntity;
import org.ardverk.dht.entity.PutEntity;
import org.ardverk.dht.entity.QuickenEntity;
import org.ardverk.dht.entity.SyncEntity;
import org.ardverk.dht.entity.ValueEntity;
import org.ardverk.dht.message.MessageFactory;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.RouteTable;
import org.ardverk.dht.storage.Database;
import org.ardverk.dht.storage.Value;


public class DefaultEasyDHT extends ArdverkDHT implements EasyDHT {
        
    private final EasyConfig config;
    
    public DefaultEasyDHT(EasyConfig config, MessageFactory messageFactory, 
            RouteTable routeTable, Database database) {
        super(messageFactory, routeTable, database);
        this.config = config;
    }
    
    @Override
    public ArdverkFuture<PingEntity> ping(String host, int port) {
        return ping(host, port, config.getPingConfig());
    }

    @Override
    public ArdverkFuture<PingEntity> ping(InetAddress address, int port) {
        return ping(address, port, config.getPingConfig());
    }

    @Override
    public ArdverkFuture<PingEntity> ping(SocketAddress address) {
        return ping(address, config.getPingConfig());
    }
    
    @Override
    public ArdverkFuture<PingEntity> ping(Contact dst) {
        return ping(dst, config.getPingConfig());
    }
    
    @Override
    public ArdverkFuture<NodeEntity> lookup(KUID lookupId) {
        return lookup(lookupId, config.getLookupConfig());
    }

    @Override
    public ArdverkFuture<ValueEntity> get(KUID valueId) {
        return get(valueId, config.getGetConfig());
    }

    @Override
    public ArdverkFuture<PutEntity> put(KUID valueId, Value value) {
        return put(valueId, value, config.getPutConfig());
    }

    @Override
    public ArdverkFuture<PutEntity> remove(KUID valueId) {
        return remove(valueId, config.getPutConfig());
    }
    
    @Override
    public ArdverkFuture<BootstrapEntity> bootstrap(String host, int port) {
        return bootstrap(host, port, config.getBootstrapConfig());
    }

    @Override
    public ArdverkFuture<BootstrapEntity> bootstrap(
            InetAddress address, int port) {
        return bootstrap(address, port, config.getBootstrapConfig());
    }

    @Override
    public ArdverkFuture<BootstrapEntity> bootstrap(SocketAddress address) {
        return bootstrap(address, config.getBootstrapConfig());
    }

    @Override
    public ArdverkFuture<BootstrapEntity> bootstrap(Contact contact) {
        return bootstrap(contact, config.getBootstrapConfig());
    }

    @Override
    public ArdverkFuture<QuickenEntity> quicken() {
        return quicken(config.getQuickenConfig());
    }
    
    @Override
    public ArdverkFuture<SyncEntity> sync() {
        return sync(config.getSyncConfig());
    }
}