package com.ardverk.dht.storage;


public abstract class AbstractDatabase implements Database {

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
}
