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

package com.ardverk.dht.utils;

import java.net.SocketAddress;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.IContact;

/**
 * The {@link ContactKey} can be used to keep track of certain operations
 * such as sending out PING requests.
 */
public class ContactKey {
    
    private final KUID contactId;
    
    private final SocketAddress address;
    
    public ContactKey(IContact contact) {
        this(contact.getId(), contact.getRemoteAddress());
    }
    
    public ContactKey(KUID contactId, SocketAddress address) {
        this.contactId = contactId;
        this.address = address;
    }
    
    @Override
    public int hashCode() {
        return 31*contactId.hashCode() + address.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ContactKey)) {
            return false;
        }
        
        ContactKey other = (ContactKey)o;
        return contactId.equals(other.contactId) 
                && address.equals(other.address);
    }
    
    @Override
    public String toString() {
        return contactId + "/" + address;
    }
}