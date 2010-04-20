package com.ardverk.dht.storage;

import java.util.concurrent.TimeUnit;

import com.ardverk.coding.CodingUtils;
import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact2;

public class DefaultValue implements Value {
    
    private final long creationTime = System.currentTimeMillis();
    
    private final Contact2 src;
    
    private final KUID key;
    
    private final byte[] value;
    
    public DefaultValue(Contact2 src, KUID key, byte[] value) {
        this.src = src;
        this.key = key;
        this.value = value;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }
    
    @Override
    public long getAge(TimeUnit unit) {
        return unit.convert(System.currentTimeMillis() - creationTime, TimeUnit.MILLISECONDS);
    }

    @Override
    public Contact2 getSource() {
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