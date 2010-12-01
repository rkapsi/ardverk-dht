package com.ardverk.dht.io;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.lang.ArdverkException;
import com.ardverk.dht.storage.ValueTuple;

public class StoreException extends ArdverkException {
    
    private static final long serialVersionUID = -1874658787780091708L;

    private final ValueTuple tuple;
    
    public StoreException(ValueTuple tuple, long time, TimeUnit unit) {
        super(time, unit);
        this.tuple = tuple;
    }

    public ValueTuple getValueTuple() {
        return tuple;
    }
}