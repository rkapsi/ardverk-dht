package com.ardverk.dht.storage;

import java.util.concurrent.TimeUnit;

/**
 * An abstract implementation of {@link Value}.
 */
abstract class AbstractValue implements Value {

    private final long creationTime = System.currentTimeMillis();
    
    @Override
    public long getCreationTime() {
        return creationTime;
    }
    
    @Override
    public long getAge(TimeUnit unit) {
        long age = System.currentTimeMillis() - creationTime;
        return unit.convert(age, TimeUnit.MILLISECONDS);
    }

    @Override
    public long getAgeInMillis() {
        return getAge(TimeUnit.MILLISECONDS);
    }
}
