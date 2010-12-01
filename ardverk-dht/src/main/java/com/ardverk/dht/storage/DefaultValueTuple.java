package com.ardverk.dht.storage;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public class DefaultValueTuple extends AbstractValueTuple {
    
    private final Contact sender;
    
    private final Contact creator;
    
    private final KUID key;
    
    private final byte[] value;
    
    public DefaultValueTuple(Contact contact, KUID key, byte[] value) {
        this (contact, contact, key, value);
    }
    
    public DefaultValueTuple(Contact sender, Contact creator, 
            KUID key, byte[] value) {
        
        this.sender = Arguments.notNull(sender, "sender");
        this.creator = Arguments.notNull(
                pickCreator(sender, creator), "creator");
        
        this.key = Arguments.notNull(key, "key");
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
    public KUID getId() {
        return key;
    }
    
    @Override
    public byte[] getValue() {
        return value;
    }

    @Override
    public int size() {
        return value != null ? value.length : 0;
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