package com.ardverk.dht.storage;

import com.ardverk.coding.CodingUtils;
import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public class DefaultValue extends AbstractValue {
    
    private final Contact src;
    
    private final KUID key;
    
    private final byte[] value;
    
    public DefaultValue(Contact src, KUID key, byte[] value) {
        this.src = src;
        this.key = key;
        this.value = value;
    }

    @Override
    public Contact getSource() {
        return src;
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
        return key + "={" + src + ", " + CodingUtils.encodeBase16(value) + "}";
    }
}