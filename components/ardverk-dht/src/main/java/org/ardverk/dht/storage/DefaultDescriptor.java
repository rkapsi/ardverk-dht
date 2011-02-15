package org.ardverk.dht.storage;

import org.ardverk.dht.KUID;
import org.ardverk.dht.routing.Contact;

public class DefaultDescriptor extends AbstractDescriptor {

    private final Contact sender;
    
    private final Contact creator;
    
    private final KUID valueId;
    
    public DefaultDescriptor(Contact contact, KUID valueId) {
        this (contact, contact, valueId);
    }
    
    public DefaultDescriptor(Contact sender, 
            Contact creator, KUID valueId) {
        this.sender = sender;
        this.creator = pickCreator(sender, creator);
        this.valueId = valueId;
    }

    @Override
    public KUID getId() {
        return valueId;
    }

    @Override
    public Contact getSender() {
        return sender;
    }
    
    @Override
    public Contact getCreator() {
        return creator;
    }
    
    /**
     * To save memory we're trying to re-use the {@link Contact}
     * instance if sender and creator are the same.
     */
    private static Contact pickCreator(Contact sender, Contact creator) {
        if (creator == null || sender.equals(creator)) {
            return sender;
        }
        
        return creator;
    }
}
