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

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncProcess;
import org.ardverk.io.IoUtils;
import org.ardverk.net.NetworkUtils;

import com.ardverk.dht.codec.DefaultMessageCodec;
import com.ardverk.dht.codec.MessageCodec;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.BootstrapConfig;
import com.ardverk.dht.config.Config;
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
import com.ardverk.dht.entity.QuickenEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.entity.SyncEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.io.transport.DatagramTransport;
import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.message.DefaultMessageFactory;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.DefaultRouteTable;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;
import com.ardverk.dht.storage.DefaultDatabase;

public class SimpleArdverkDHT implements DHT, Closeable {
    
    private final SimpleConfig config;
    
    private final ArdverkDHT dht;
    
    public SimpleArdverkDHT(SimpleConfig config, int port) throws IOException {
        this(config, new DatagramTransport(port));
    }
    
    public SimpleArdverkDHT(SimpleConfig config, 
            String address, int port) throws IOException {
        this(config, new DatagramTransport(address, port));
    }
    
    public SimpleArdverkDHT(SimpleConfig config, 
            InetAddress address, int port) throws IOException {
        this(config, new DatagramTransport(address, port));
    }
    
    public SimpleArdverkDHT(SimpleConfig config, 
            SocketAddress address) throws IOException {
        this(config, new DatagramTransport(address));
    }
    
    public SimpleArdverkDHT(SimpleConfig config, 
            Transport transport) throws IOException {
        this.config = config;
        
        int keySize = config.getKeySize();
        SocketAddress address = config.getAddress(transport);
        
        Contact localhost = Contact.localhost(
                KUID.createRandom(keySize), address);
        
        String secretKey = config.getSecretKey();
        String initVector = config.getInitVector();
        MessageCodec codec = new DefaultMessageCodec(secretKey, initVector);
        
        MessageFactory messageFactory 
            = new DefaultMessageFactory(keySize, localhost);
        
        Database database = new DefaultDatabase();
        RouteTable routeTable = new DefaultRouteTable(localhost);
        
        dht = new ArdverkDHT(codec, messageFactory, routeTable, database);
        bind(transport);
    }
    
    public ArdverkDHT getArdverkDHT() {
        return dht;
    }

    @Override
    public void close() {
        IoUtils.close(dht);
    }
    
    @Override
    public <V> ArdverkFuture<V> submit(AsyncProcess<V> process, Config config) {
        return dht.submit(process, config);
    }

    @Override
    public <V> ArdverkFuture<V> submit(QueueKey queueKey,
            AsyncProcess<V> process, long timeout, TimeUnit unit) {
        return dht.submit(queueKey, process, timeout, unit);
    }

    @Override
    public Contact getLocalhost() {
        return dht.getLocalhost();
    }

    @Override
    public RouteTable getRouteTable() {
        return dht.getRouteTable();
    }

    @Override
    public Database getDatabase() {
        return dht.getDatabase();
    }
    
    @Override
    public void bind(Transport t) throws IOException {
        dht.bind(t);
    }

    @Override
    public void unbind() {
        dht.unbind();
    }

    @Override
    public boolean isBound() {
        return dht.isBound();
    }

    public ArdverkFuture<PingEntity> ping(String host, int port) {
        return ping(host, port, config.getPingConfig());
    }

    public ArdverkFuture<PingEntity> ping(InetAddress address, int port) {
        return ping(address, port, config.getPingConfig());
    }

    public ArdverkFuture<PingEntity> ping(SocketAddress address) {
        return ping(address, config.getPingConfig());
    }
    
    public ArdverkFuture<PingEntity> ping(Contact dst) {
        return ping(dst, config.getPingConfig());
    }
    
    @Override
    public ArdverkFuture<PingEntity> ping(String host, int port, PingConfig config) {
        return dht.ping(host, port, config);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(InetAddress address, int port,
            PingConfig config) {
        return dht.ping(address, port, config);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(SocketAddress address,
            PingConfig config) {
        return dht.ping(address, config);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(Contact dst, PingConfig config) {
        return dht.ping(dst, config);
    }

    public ArdverkFuture<NodeEntity> lookup(KUID lookupId) {
        return lookup(lookupId, config.getLookupConfig());
    }
    
    @Override
    public ArdverkFuture<NodeEntity> lookup(KUID lookupId, LookupConfig config) {
        return dht.lookup(lookupId, config);
    }

    public ArdverkFuture<ValueEntity> get(KUID key) {
        return get(key, config.getGetConfig());
    }
    
    @Override
    public ArdverkFuture<ValueEntity> get(KUID key, GetConfig config) {
        return dht.get(key, config);
    }

    public ArdverkFuture<StoreEntity> put(KUID key, byte[] value) {
        return put(key, value, config.getPutConfig());
    }
    
    @Override
    public ArdverkFuture<StoreEntity> put(KUID key, byte[] value,
            PutConfig config) {
        return dht.put(key, value, config);
    }

    public ArdverkFuture<StoreEntity> remove(KUID key) {
        return remove(key, config.getPutConfig());
    }
    
    @Override
    public ArdverkFuture<StoreEntity> remove(KUID key, PutConfig config) {
        return dht.remove(key, config);
    }
    
    public ArdverkFuture<BootstrapEntity> bootstrap(String host, int port) {
        return bootstrap(host, port, config.getBootstrapConfig());
    }

    public ArdverkFuture<BootstrapEntity> bootstrap(
            InetAddress address, int port) {
        return bootstrap(address, port, config.getBootstrapConfig());
    }

    public ArdverkFuture<BootstrapEntity> bootstrap(SocketAddress address) {
        return bootstrap(address, config.getBootstrapConfig());
    }

    public ArdverkFuture<BootstrapEntity> bootstrap(Contact contact) {
        return dht.bootstrap(contact, config.getBootstrapConfig());
    }
    
    @Override
    public ArdverkFuture<BootstrapEntity> bootstrap(String host, int port,
            BootstrapConfig config) {
        return dht.bootstrap(host, port, config);
    }

    @Override
    public ArdverkFuture<BootstrapEntity> bootstrap(InetAddress address,
            int port, BootstrapConfig config) {
        return dht.bootstrap(address, port, config);
    }

    @Override
    public ArdverkFuture<BootstrapEntity> bootstrap(SocketAddress address,
            BootstrapConfig config) {
        return dht.bootstrap(address, config);
    }

    @Override
    public ArdverkFuture<BootstrapEntity> bootstrap(Contact contact,
            BootstrapConfig config) {
        return dht.bootstrap(contact, config);
    }

    public ArdverkFuture<QuickenEntity> quicken() {
        return quicken(config.getQuickenConfig());
    }
    
    @Override
    public ArdverkFuture<QuickenEntity> quicken(QuickenConfig config) {
        return dht.quicken(config);
    }
    
    public ArdverkFuture<SyncEntity> sync() {
        return sync(config.getSyncConfig());
    }
    
    @Override
    public ArdverkFuture<SyncEntity> sync(SyncConfig config) {
        return dht.sync(config);
    }
    
    public static class SimpleConfig {
        
        private static final int DEFAULT_KEY_SIZE = 20;
        
        private final int keySize;
        
        private volatile PingConfig pingConfig = new DefaultPingConfig();
        
        private volatile LookupConfig lookupConfig = new DefaultLookupConfig();
        
        private volatile GetConfig getConfig = new DefaultGetConfig();
        
        private volatile PutConfig putConfig = new DefaultPutConfig();
        
        private volatile BootstrapConfig bootstrapConfig = new DefaultBootstrapConfig();
        
        private volatile QuickenConfig quickenConfig = new DefaultQuickenConfig();
        
        private volatile SyncConfig syncConfig = new DefaultSyncConfig();
        
        private volatile SocketAddress address = null;
        
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

        public SocketAddress getAddress() {
            return address;
        }

        public void setAddress(SocketAddress address) {
            this.address = address;
        }
        
        SocketAddress getAddress(Transport transport) {
            if (address != null) {
                return address;
            }
            
            return extract(transport.getSocketAddress());
        }
        
        private static SocketAddress extract(SocketAddress address) {
            InetAddress addr = NetworkUtils.getAddress(address);
            if (!NetworkUtils.isPrivateAddress(addr)) {
                return address;
            }
            
            return new InetSocketAddress("localhost", NetworkUtils.getPort(address));
        }
    }
}
