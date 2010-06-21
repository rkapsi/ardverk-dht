package com.ardverk.dht.storage;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public class DefaultValueTuple extends AbstractValueTuple {
    
    private final Contact sender;
    
    private final Contact creator;
    
    private final Key key;
    
    private final Value value;
    
    public DefaultValueTuple(Contact contact, KUID key, byte[] value) {
        this (contact, new DefaultKey(key), new DefaultValue(value));
    }
    
    public DefaultValueTuple(Contact contact, Key key, Value value) {
        this (contact, contact, key, value);
    }
    
    public DefaultValueTuple(Contact sender, Contact creator, 
            Key key, Value value) {
        
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
    public Key getKey() {
        return key;
    }
    
    @Override
    public Value getValue() {
        return value;
    }
    
    @Override
    public boolean isEmpty() {
        return value.isEmpty();
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