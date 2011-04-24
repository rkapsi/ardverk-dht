package org.ardverk.dht.storage;

import org.ardverk.dht.routing.Contact;

public interface Resource {

    public ResourceId getResourceId();
    
    public Contact getSender();
}
