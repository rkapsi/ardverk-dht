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
    public void handleContactAdded(Bucket bucket, Contact contact) {
        handleContact(contact);
    }

    @Override
    public void handleContactReplaced(Bucket bucket, Contact existing,
            Contact contact) {
        handleContact(contact);
    }

    @Override
    public void handleContactChanged(Bucket bucket, Contact existing,
            Contact contact) {
        handleContact(contact);
    }

    public void handleRequest(Contact contact) {
        handleContact(contact);
    }
    
    public void handleResponse(Contact contact) {
        handleContact(contact);
    }
    
    public void handleLateResponse(Contact contact) {
        handleContact(contact);
    }
    
    private void handleContact(Contact contact) {
        KUID contactId = contact.getId();

        Contact localhost = routeTable.getLocalhost();
        Contact[] contacts = routeTable.select(contactId);
        if (!isResponsible(localhost, contact, contacts)) {
            return;
        }
        
        Contact last = CollectionUtils.last(contacts);    
        forward(contact, last.getId());
    }
    
    protected void forward(Contact contact, KUID lastId) {
    }
    
    /**
     * Returns {@code true} if we're responsible for store-forwarding
     * a value to the given {@link Contact}.
     */
    private static boolean isResponsible(Contact localhost, 
            Contact contact, Contact[] contacts) {
        
        if (0 < contacts.length && !contact.equals(localhost)) {
            Contact first = CollectionUtils.first(contacts);
            
            // The contact isn't in our Route Table yet.
            if (first.equals(localhost)) {
                return true;
            }
            
            // The contact is in our Route Table, we're the second
            // closest to it and its instance ID has changed.
            if (1 < contacts.length && first.equals(contact)) {
                Contact second = CollectionUtils.nth(contacts, 1);
                if (second.equals(localhost) 
                        && isNewOrHasChanged(contact, first)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Returns {@code true} if the given {@link Contact} is either new
     * or if has changed (i.e. its instance ID has changed).
     */
    private static boolean isNewOrHasChanged(Contact contact, Contact existing) {
        if (existing != null) {
            return contact.getInstanceId() != existing.getInstanceId();
        }
        return true;
    }
}