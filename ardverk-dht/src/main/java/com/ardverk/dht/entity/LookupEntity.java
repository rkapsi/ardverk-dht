package com.ardverk.dht.entity;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.KUID;

public class LookupEntity extends AbstractEntity {

    public AsyncFuture<StoreEntity> store(KUID key, byte[] value) {
        return null;
    }
}
