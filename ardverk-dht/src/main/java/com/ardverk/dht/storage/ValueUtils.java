package com.ardverk.dht.storage;

import java.util.concurrent.atomic.AtomicInteger;

public class ValueUtils {

    private static final AtomicInteger ID = new AtomicInteger();
    
    private ValueUtils() {}
    
    public static int createId() {
        int value = 0;
        while ((value = ID.incrementAndGet()) == 0);
        return value;
    }
}
