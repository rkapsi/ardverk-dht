package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

public class DefaultValueEntity extends AbstractEntity implements ValueEntity {

    public DefaultValueEntity(long time, TimeUnit unit) {
        super(time, unit);
    }
}
