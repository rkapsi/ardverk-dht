package com.ardverk.dht.storage;

import com.ardverk.coding.CodingUtils;
import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public class DefaultValue extends AbstractValue {
    
    private final KUID identifier;
    
    private final Contact sender;
    
    private final Contact creator;
    
    private final KUID key;
    
    private final byte[] value;
    
    public DefaultValue(Contact contact, KUID key, byte[] value) {
        this (contact, contact, key, value);
    }
    
    public DefaultValue(Contact sender, Contact creator, KUID key, byte[] value) {
        this.identifier = KUID.createRandom(key.length());

        this.sender = sender;
        this.creator = creator;
        this.key = key;
        this.value = value;
    }
    
    @Override
    public KUID getIdentifier() {
        return identifier;
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
    public KUID getKey() {
        return key;
    }

    @Override
    public byte[] getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return key + "={" + sender + ", " + CodingUtils.encodeBase16(value) + "}";
    }
}