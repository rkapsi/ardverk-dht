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

package org.ardverk.dht.storage;

import java.util.concurrent.atomic.AtomicReference;

import org.ardverk.dht.ArdverkDHT;
import org.ardverk.dht.DHT;
import org.ardverk.dht.KUID;
import org.ardverk.dht.StoreManager;
import org.ardverk.dht.config.StoreConfig;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.RouteTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract implementation of {@link Database}.
 */
public abstract class AbstractDatabase implements Database {
    
    private static final Logger LOG 
        = LoggerFactory.getLogger(AbstractDatabase.class);
    
    private final AtomicReference<DHT> dhtRef = new AtomicReference<DHT>();
    
    protected DHT getDHT() {
        return dhtRef.get();
    }
    
    @Override
    public void bind(DHT dht) {
        dhtRef.set(dht);
    }

    @Override
    public boolean isBound() {
        return dhtRef.get() != null;
    }

    @Override
    public void unbind() {
        dhtRef.set(null);
    }
    
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    
    @Override
    public void forward(Contact dst, KUID lastId) {
        DatabaseConfig config = getDatabaseConfig();
        if (!config.isStoreForward()) {
            return;
        }
        
        StoreConfig storeConfig = config.getStoreConfig();
        Iterable<Key> keys = values(dst.getId(), lastId);
        
        DHT dht = getDHT();
        StoreManager storeManager = ((ArdverkDHT)dht).getStoreManager();
        
        for (Key key : keys) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(dht.getLocalhost().getId() 
                        + " forward " + key
                        + " to " + dst.getId());
            }
            
            Value value = get(key);
            if (value != null) {
                storeManager.store(new Contact[] { dst }, 
                        key, value, storeConfig);
            }
        }
    }
    
    protected boolean isInBucket(Key key) {
        DatabaseConfig config = getDatabaseConfig();
        if (config.isCheckBucket()) {
            RouteTable routeTable = getDHT().getRouteTable();
            return DatabaseUtils.isInBucket(key, routeTable);
        }
        return true;
    }
}