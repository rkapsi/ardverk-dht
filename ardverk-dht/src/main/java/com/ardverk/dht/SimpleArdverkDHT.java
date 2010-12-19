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

import com.ardverk.dht.codec.DefaultMessageCodec;
import com.ardverk.dht.codec.MessageCodec;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.BootstrapConfig;
import com.ardverk.dht.config.DefaultBootstrapConfig;
import com.ardverk.dht.config.DefaultGetConfig;
import com.ardverk.dht.config.DefaultLookupConfig;
import com.ardverk.dht.config.DefaultPingConfig;
import com.ardverk.dht.config.DefaultPutConfig;
import com.ardverk.dht.config.DefaultQuickenConfig;
import com.ardverk.dht.config.DefaultSyncConfig;
import com.ardverk.dht.config.GetConfig;
import com.ardverk.dht.config.LookupConfig;
import com.ardverk.dht.config.PingConfig;
import com.ardverk.dht.config.PutConfig;
import com.ardverk.dht.config.QuickenConfig;
import com.ardverk.dht.config.SyncConfig;
import com.ardverk.dht.entity.BootstrapEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.PutEntity;
import com.ardverk.dht.entity.QuickenEntity;
import com.ardverk.dht.entity.SyncEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.io.transport.DatagramTransport;
import com.ardverk.dht.message.DefaultMessageFactory;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.DefaultRouteTable;
import com.ardverk.dht.routing.Localhost;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;
import com.ardverk.dht.storage.DefaultDatabase;

public class SimpleArdverkDHT extends ArdverkDHT implements SimpleDHT {
        
    public static SimpleArdverkDHT create() {
        return create(new SimpleConfig());
    }
    
    public static SimpleArdverkDHT create(SimpleConfig config) {
        int keySize = config.getKeySize();
        Localhost localhost = new Localhost(keySize);
        
        String secretKey = config.getSecretKey();
        String initVector = config.getInitVector();
        
        MessageCodec codec = null;
        if (secretKey != null && initVector != null) {
            codec = new DefaultMessageCodec(secretKey, initVector);
        } else {
            codec = new DefaultMessageCodec();
        }
        
        MessageFactory messageFactory 
            = new DefaultMessageFactory(keySize, localhost);
        
        Database database = new DefaultDatabase();
        RouteTable routeTable = new DefaultRouteTable(localhost);
        
        return new SimpleArdverkDHT(config, codec, 
                messageFactory, routeTable, database);
    }
    
    private final SimpleConfig config;
    
    public SimpleArdverkDHT(SimpleConfig config, MessageCodec codec, 
            MessageFactory messageFactory, RouteTable routeTable, 
            Database database) {
        super(codec, messageFactory, routeTable, database);
        
        this.config = config;
    }
    
    @Override
    public void bind(int port) throws IOException {
        bind(new DatagramTransport(port));
    }
    
    @Override
    public void bind(String host, int port) throws IOException {
        bind(new DatagramTransport(host, port));
    }
    
    @Override
    public void bind(InetAddress bindaddr, int port) throws IOException {
        bind(new DatagramTransport(bindaddr, port));
    }
    
    @Override
    public void bind(SocketAddress address) throws IOException {
        bind(new DatagramTransport(address));
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
    public ArdverkFuture<ValueEntity> get(KUID key) {
        return get(key, config.getGetConfig());
    }

    @Override
    public ArdverkFuture<PutEntity> put(KUID key, byte[] value) {
        return put(key, value, config.getPutConfig());
    }

    @Override
    public ArdverkFuture<PutEntity> remove(KUID key) {
        return remove(key, config.getPutConfig());
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
    
    public static class SimpleConfig {
        
        private static final int DEFAULT_KEY_SIZE = 20; // SHA-1
        
        private final int keySize;
        
        private volatile PingConfig pingConfig = new DefaultPingConfig();
        
        private volatile LookupConfig lookupConfig = new DefaultLookupConfig();
        
        private volatile GetConfig getConfig = new DefaultGetConfig();
        
        private volatile PutConfig putConfig = new DefaultPutConfig();
        
        private volatile BootstrapConfig bootstrapConfig = new DefaultBootstrapConfig();
        
        private volatile QuickenConfig quickenConfig = new DefaultQuickenConfig();
        
        private volatile SyncConfig syncConfig = new DefaultSyncConfig();
        
        private volatile String secretKey = null;
        
        private volatile String initVector = null;
        
        public SimpleConfig() {
            this(DEFAULT_KEY_SIZE);
        }
        
        public SimpleConfig(int keySize) {
            this.keySize = keySize;
        }

        public int getKeySize() {
            return keySize;
        }
        
        public PingConfig getPingConfig() {
            return pingConfig;
        }

        public void setPingConfig(PingConfig pingConfig) {
            this.pingConfig = pingConfig;
        }

        public LookupConfig getLookupConfig() {
            return lookupConfig;
        }

        public void setLookupConfig(LookupConfig lookupConfig) {
            this.lookupConfig = lookupConfig;
        }

        public GetConfig getGetConfig() {
            return getConfig;
        }

        public void setGetConfig(GetConfig getConfig) {
            this.getConfig = getConfig;
        }

        public PutConfig getPutConfig() {
            return putConfig;
        }

        public void setPutConfig(PutConfig putConfig) {
            this.putConfig = putConfig;
        }

        public BootstrapConfig getBootstrapConfig() {
            return bootstrapConfig;
        }

        public void setBootstrapConfig(BootstrapConfig bootstrapConfig) {
            this.bootstrapConfig = bootstrapConfig;
        }

        public QuickenConfig getQuickenConfig() {
            return quickenConfig;
        }

        public void setQuickenConfig(QuickenConfig quickenConfig) {
            this.quickenConfig = quickenConfig;
        }

        public SyncConfig getSyncConfig() {
            return syncConfig;
        }

        public void setSyncConfig(SyncConfig syncConfig) {
            this.syncConfig = syncConfig;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getInitVector() {
            return initVector;
        }

        public void setInitVector(String initVector) {
            this.initVector = initVector;
        }
    }
}