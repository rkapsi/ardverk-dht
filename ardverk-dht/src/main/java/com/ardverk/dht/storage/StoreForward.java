package com.ardverk.dht.storage;

import org.ardverk.collection.CollectionUtils;

import com.ardverk.dht.KUID;
import com.ardverk.dht.QueueKey;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.DefaultStoreConfig;
import com.ardverk.dht.config.StoreConfig;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;

public class StoreForward {

    private volatile StoreConfig storeConfig = new DefaultStoreConfig();
    
    private final Callback callback;
    
    private final RouteTable routeTable;
    
    private final Database database;
    
    // INIT
    {
        storeConfig.setQueueKey(QueueKey.BACKEND);
    }
    
    public StoreForward(Callback callback, 
            RouteTable routeTable, Database database) {
        this.callback = callback;
        this.routeTable = routeTable;
        this.database = database;
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
        if (callback == null) {
            return;
        }
        
        KUID contactId = contact.getId();
        Contact existing = routeTable.get(contactId);
        
        for (ValueTuple tuple : database.values()) {
            KUID valueId = tuple.getId();
            Contact[] contacts = routeTable.select(valueId);
            
            // If there are more than K-contacts available then make
            // sure the new Contact is closer to the value than the
            // furthest of the current Contacts.
            if (contacts.length >= routeTable.getK()) {
                Contact furthest = CollectionUtils.last(contacts);
                if (!contactId.isCloserTo(valueId, furthest.getId())
                    && !furthest.equals(contact)) {
                    continue;
                }
            }
            
            // And we must be responsible for forwarding it.
            if (!isResponsible(contact, existing, contacts)) {
                continue;
            }
            
            System.out.println("Forward: " + tuple.getId() + " to " + contact.getId());
            callback.store(contact, tuple, storeConfig);
        }
    }
    
    private boolean isResponsible(Contact contact, Contact existing, Contact[] contacts) {
        if (0 < contacts.length && isNewOrHasChanged(contact, existing)) {
            Contact localhost = routeTable.getLocalhost();
            Contact first = CollectionUtils.first(contacts);
            if (first.equals(localhost)) {
                return true;
            }
            
            if (1 < contacts.length) {
                Contact second = CollectionUtils.nth(contacts, 1);
                if (second.equals(localhost)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static boolean isNewOrHasChanged(Contact contact, Contact existing) {
        if (existing == null) {
            return true;
        }
        
        if (!contact.equals(existing)) {
            return false;
        }
        
        return contact.getInstanceId() != existing.getInstanceId();
    }
    
    public static interface Callback {
        
        public ArdverkFuture<StoreEntity> store(Contact dst, 
                ValueTuple valueTuple, StoreConfig config);
    }
}
