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

package com.ardverk.dht.storage;

import org.ardverk.collection.CollectionUtils;

import com.ardverk.dht.KUID;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.StoreConfig;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.utils.IdentifierUtils;

/**
 * This class implements the logic that is used to determinate weather or
 * not a value should be store-forwarded.
 */
public class StoreForward {

    private final RouteTable routeTable;
    
    private final Database database;
    
    private volatile Callback callback;
    
    public StoreForward(RouteTable routeTable, Database database) {
        this.routeTable = routeTable;
        this.database = database;
    }
    
    public void bind(Callback callback) {
        this.callback = callback;
    }
    
    public void unbind() {
        this.callback = null;
    }
    
    public boolean isBound() {
        return callback != null;
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
        DatabaseConfig config = database.getDatabaseConfig();
        if (!config.isStoreForward()) {
            return;
        }
        
        Callback callback = this.callback;
        if (callback == null) {
            return;
        }
        
        if (contact.isInvisible()) {
            return;
        }
        
        StoreConfig storeConfig = config.getStoreConfig();
        
        KUID contactId = contact.getId();
        Contact existing = routeTable.get(contactId);
        
        for (ValueTuple tuple : database.values()) {
            KUID valueId = tuple.getId();
            Contact[] contacts = routeTable.select(valueId);
            
            // Check if the Contact is closer to the value than
            // the furthest of the current Contacts.
            if (!isCloserThanFurthest(valueId, contact, contacts)) {
                continue;
            }
            
            // And we must be responsible for forwarding it.
            if (!isResponsible(contact, existing, contacts)) {
                continue;
            }
            
            //System.out.println(routeTable.getLocalhost().getId() 
            //        + " foward " + tuple.getId() + " to " + contact.getId());
            callback.store(contact, tuple, storeConfig);
        }
    }
    
    /**
     * Returns {@code true} if the new {@link Contact} is closer to
     * the given {@link KUID} than the furthest of our current k-closest
     * {@link Contact}s.
     */
    private boolean isCloserThanFurthest(KUID valueId, 
            Contact contact, Contact[] contacts) {
        
        if (contacts.length >= routeTable.getK()) {
            Contact furthest = CollectionUtils.last(contacts);
            
            if (!IdentifierUtils.isCloserTo(contact, valueId, furthest) 
                    && !furthest.equals(contact)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Returns {@code true} if we're responsible for store-forwarding
     * a value to the given {@link Contact}.
     */
    private boolean isResponsible(Contact contact, 
            Contact existing, Contact[] contacts) {
        
        if (0 < contacts.length && isNewOrHasChanged(contact, existing)) {
            Contact localhost = routeTable.getLocalhost();
            Contact first = CollectionUtils.first(contacts);
            if (first.equals(localhost)) {
                return true;
            }
            
            if (1 < contacts.length && first.equals(contact)) {
                Contact second = CollectionUtils.nth(contacts, 1);
                if (second.equals(localhost)) {
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
    
    /**
     * The {@link Callback} is being called by the 
     * {@link StoreForward} service.
     * 
     * @see StoreForward#bind(Callback)
     */
    public static interface Callback {
        
        /**
         * Called by the {@link StoreForward} service for each {@link ValueTuple}
         * that needs to be sent to the given {@link Contact}.
         */
        public ArdverkFuture<StoreEntity> store(Contact dst, 
                ValueTuple valueTuple, StoreConfig config);
    }
}