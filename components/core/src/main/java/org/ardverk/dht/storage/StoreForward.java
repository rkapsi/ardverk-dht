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

import org.ardverk.collection.CollectionUtils;
import org.ardverk.dht.KUID;
import org.ardverk.dht.routing.Bucket;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.RouteTable;
import org.ardverk.dht.routing.RouteTableAdapter;


/**
 * This class implements the logic that is used to determinate weather or
 * not a value should be store-forwarded.
 */
public class StoreForward extends RouteTableAdapter {
    
    protected final RouteTable routeTable;
    
    public StoreForward(RouteTable routeTable) {
        this.routeTable = routeTable;
    }
    
    @Override
    public void handleContact(Bucket bucket, Contact existing, Contact contact) {
        // Don't do anything if the Contact was already in our RT and its
        // instance ID hasn't changed.
        if (existing != null && existing.equals(contact) 
                && existing.getInstanceId() == contact.getInstanceId()) {
            return;
        }
        
        // Make sure we're never attempting to store-forward to ourselves.
        Contact localhost = routeTable.getLocalhost();
        if (contact.equals(localhost)) {
            return;
        }
        
        KUID contactId = contact.getId();
        Contact[] contacts = routeTable.select(contactId);
        
        // There must be at least two Contacts. Us and somebody else.
        if (contacts.length < 2) {
            return;
        }
        
        // The 'somebody else' is actually that Contact and we must
        // be the closest to it. If we aren't then it's some other
        // Node's responsibility to do the store-forwarding!
        if (!contacts[1].equals(localhost)) {
            return;
        }
        
        assert (contacts[0].equals(contact));
        
        Contact last = CollectionUtils.last(contacts);    
        forward(contact, last.getId());
    }
    
    protected void forward(Contact contact, KUID lastId) {
    }
}