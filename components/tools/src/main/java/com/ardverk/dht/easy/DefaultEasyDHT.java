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

package com.ardverk.dht.easy;

import java.net.InetAddress;
import java.net.SocketAddress;

import com.ardverk.dht.ArdverkDHT;
import com.ardverk.dht.KUID;
import com.ardverk.dht.codec.MessageCodec;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.BootstrapEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.PutEntity;
import com.ardverk.dht.entity.QuickenEntity;
import com.ardverk.dht.entity.SyncEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;
import com.ardverk.dht.storage.Value;

public class DefaultEasyDHT extends ArdverkDHT implements EasyDHT {
        
    private final EasyConfig config;
    
    public DefaultEasyDHT(EasyConfig config, MessageCodec codec, 
            MessageFactory messageFactory, RouteTable routeTable, 
            Database database) {
        super(codec, messageFactory, routeTable, database);
        
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