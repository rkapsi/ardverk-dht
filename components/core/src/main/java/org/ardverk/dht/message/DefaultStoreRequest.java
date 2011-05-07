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

package org.ardverk.dht.message;

import java.net.SocketAddress;

import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.Value;

public class DefaultStoreRequest extends AbstractRequestMessage 
        implements StoreRequest {

    private final Key key;
    
    public DefaultStoreRequest(MessageId messageId, Contact contact, 
            SocketAddress address, Key key, Value value) {
        super(messageId, contact, address, value);
        
        this.key = key;
    }
    
    @Override
    public Key getKey() {
        return key;
    }
}