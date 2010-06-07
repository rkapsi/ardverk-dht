package com.ardverk.dht.storage;

import static com.ardverk.coding.CodingUtils.encodeBase16;

import org.ardverk.lang.NullArgumentException;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public class DefaultValue extends AbstractValue {
    
    private final byte[] id;
    
    private final Contact sender;
    
    private final Contact creator;
    
    private final KUID primaryKey;
    
    private final byte[] value;
    
    public DefaultValue(Contact contact, KUID primaryKey, byte[] value) {
        this (contact, contact, primaryKey, value);
    }
    
    public DefaultValue(Contact sender, Contact creator, 
            KUID primaryKey, byte[] value) {
        this (sender, creator, ValueUtils.createId(primaryKey, value), 
                primaryKey, value);
    }
    
    public DefaultValue(Contact sender, Contact creator, 
            byte[] id, KUID primaryKey, byte[] value) {
        
        if (sender == null) {
            throw new NullArgumentException("sender");
        }
        
        if (creator == null) {
            throw new NullArgumentException("creator");
        }
        
        if (id == null) {
            throw new NullArgumentException("id");
        }
        
        if (primaryKey == null) {
            throw new NullArgumentException("primaryKey");
        }
        
        if (value == null) {
            throw new NullArgumentException("value");
        }
        
        this.sender = sender;
        this.creator = pickCreator(sender, creator);
        this.id = id;
        this.primaryKey = primaryKey;
        this.value = value;
    }
    
    @Override
    public byte[] getId() {
        return id;
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
        return primaryKey;
    }
    
    @Override
    public KUID getSecondaryKey() {
        return creator.getContactId();
    }

    @Override
    public byte[] getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return primaryKey + "={" + encodeBase16(id) + ", " + sender 
            + ", " + creator + ", " + encodeBase16(value) + "}";
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