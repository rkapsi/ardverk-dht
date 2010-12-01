package com.ardverk.dht.storage;

import java.util.concurrent.TimeUnit;

/**
 * An abstract implementation of {@link ValueTuple}.
 */
abstract class AbstractValueTuple implements ValueTuple {

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

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
}
