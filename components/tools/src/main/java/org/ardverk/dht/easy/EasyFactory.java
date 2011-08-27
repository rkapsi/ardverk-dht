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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardverk.dht.message.DefaultMessageFactory;
import org.ardverk.dht.message.MessageFactory;
import org.ardverk.dht.routing.DefaultRouteTable;
import org.ardverk.dht.routing.Localhost;
import org.ardverk.dht.routing.RouteTable;
import org.ardverk.dht.storage.Datastore;
import org.ardverk.dht.storage.IndexDatastore;
import org.ardverk.dht.storage.SimpleDatastore;

public class EasyFactory {
    
    public static EasyDHT create() {
        return create(new EasyConfig());
    }
    
    public static EasyDHT create(EasyConfig config) {
        int keySize = config.getKeySize();
        Localhost localhost = new Localhost(keySize);
        
        MessageFactory messageFactory 
            = new DefaultMessageFactory(keySize, localhost);
        
        Datastore datastore = createDatabase();
        //Database datastore = new SimpleDatabase(new File("datastore", localhost.getId().toHexString()));
        RouteTable routeTable = new DefaultRouteTable(localhost);
        
        return new DefaultEasyDHT(config, messageFactory, 
                routeTable, datastore);
    }
    
    private EasyFactory() {}
    
    
    private static Datastore createDatabase() {
        /*try {
            return new IndexDatastore();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }*/
        //return new ObjectDatastore();
        
        return new SimpleDatastore(-1L, TimeUnit.MILLISECONDS);
    }
}