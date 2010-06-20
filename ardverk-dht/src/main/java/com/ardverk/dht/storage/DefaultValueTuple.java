package com.ardverk.dht.storage;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public class DefaultValueTuple extends AbstractValueTuple {
    
    private final Contact sender;
    
    private final Contact creator;
    
    private final ValueX value;
    
    public DefaultValueTuple(Contact contact, KUID key, byte[] value) {
        this (contact, new DefaultValueX(key, value));
    }
    
    public DefaultValueTuple(Contact contact, ValueX value) {
        this (contact, contact, value);
    }
    
    public DefaultValueTuple(Contact sender, Contact creator, ValueX value) {
        
        this.sender = Arguments.notNull(sender, "sender");
        this.creator = Arguments.notNull(
                pickCreator(sender, creator), "creator");
        
        this.value = Arguments.notNull(value, "value");
    }

    @Override
    public Contact getSender() {
        return sender;
    }
    
    @Override
    public Contact getCreator() {
        return creator;
    }

    @Override
    public KUID getPrimaryKey() {
        return value.getKey();
    }
    
    @Override
    public KUID getSecondaryKey() {
        return creator.getContactId();
    }
    
    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }
    
    @Override
    public ValueX getValue() {
        return value;
    }
    
    /**
     * To save memory we're trying to re-use the {@link Contact}
     * instance if sender and creator are the same.
     */
    private static Contact pickCreator(Contact sender, Contact creator) {
        if (sender.equals(creator)) {
            return sender;
        }
        
        return creator;
    }
}