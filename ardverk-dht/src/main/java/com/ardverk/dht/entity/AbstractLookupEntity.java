package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;

abstract class AbstractLookupEntity extends AbstractEntity 
        implements LookupEntity {

    private final KUID lookupId;
    
    public AbstractLookupEntity(KUID lookupId, long time, TimeUnit unit) {
        super(time, unit);
        this.lookupId = lookupId;
    }

    @Override
    public KUID getId() {
        return lookupId;
    }
}
