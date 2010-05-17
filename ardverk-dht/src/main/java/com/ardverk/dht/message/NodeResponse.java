package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact;

/**
 * 
 */
public interface NodeResponse extends LookupResponse {

    /**
     * 
     */
    public Contact[] getContacts();
}
