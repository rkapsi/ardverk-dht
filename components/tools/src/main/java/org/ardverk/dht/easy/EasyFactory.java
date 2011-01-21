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

import org.ardverk.dht.message.DefaultMessageFactory;
import org.ardverk.dht.message.MessageFactory;
import org.ardverk.dht.routing.DefaultRouteTable;
import org.ardverk.dht.routing.Localhost;
import org.ardverk.dht.routing.RouteTable;
import org.ardverk.dht.storage.Database;
import org.ardverk.dht.storage.DefaultDatabase;

public class EasyFactory {
    
    public static EasyDHT create() {
        return create(new EasyConfig());
    }
    
    public static EasyDHT create(EasyConfig config) {
        int keySize = config.getKeySize();
        Localhost localhost = new Localhost(keySize);
        
        MessageFactory messageFactory 
            = new DefaultMessageFactory(keySize, localhost);
        
        Database database = new DefaultDatabase();
        RouteTable routeTable = new DefaultRouteTable(localhost);
        
        return new DefaultEasyDHT(config, messageFactory, 
                routeTable, database);
    }
    
    private EasyFactory() {}
}
