package org.ardverk.dht.storage;

import java.util.concurrent.TimeUnit;

import org.ardverk.lang.TimeStamp;

public abstract class AbstractDescriptor implements Descriptor {

    private final TimeStamp creationTime = TimeStamp.now();
    
    @Override
    public long getCreationTime() {
        return creationTime.getCreationTime();
    }
    
    @Override
    public long getAge(TimeUnit unit) {
        return creationTime.getAge(unit);
    }

    @Override
    public long getAgeInMillis() {
        return creationTime.getAgeInMillis();
    }
}
