package com.ardverk.dht.storage;

import java.util.Arrays;

import org.ardverk.coding.CodingUtils;
import org.ardverk.lang.Arguments;

import com.ardverk.dht.KUID;

public class DefaultValue extends AbstractValue {

    private final byte[] id;
    
    private final KUID key;
    
    private final byte[] value;
    
    public DefaultValue(KUID key, byte[] value) {
        this (ValueUtils.createId(key, value), 
                key, value);
    }
    
    public DefaultValue(byte[] id, KUID key, byte[] value) {
        this.id = id;
        this.key = Arguments.notNull(key, "key");
        this.value = Arguments.notNull(value, "value");
    }
    
    @Override
    public byte[] getId() {
        return id;
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
    public int size() {
        return value != null ? value.length : 0;
    }
    
    public String getValueAsString() {
        return CodingUtils.encodeBase16(value);
    }
    
    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode += 31 * Arrays.hashCode(id);
        hashCode += 31 * key.hashCode();
        hashCode += 31 * Arrays.hashCode(value);
        return hashCode;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Value)) {
            return false;
        }
        
        Value other = (Value)o;
        return Arrays.equals(id, other.getId())
           && key.equals(other.getKey())
           && Arrays.equals(value, other.getValue());
    }
    
    @Override
    public String toString() {
        return CodingUtils.encodeBase16(id) + "/" 
                + key + "=" + getValueAsString();
    }
}
