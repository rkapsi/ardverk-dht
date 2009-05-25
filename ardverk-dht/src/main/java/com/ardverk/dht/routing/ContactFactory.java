package com.ardverk.dht.routing;

import java.io.Serializable;

import com.ardverk.dht.KeyFactory;

public interface ContactFactory extends Serializable {

    public KeyFactory getKeyFactory();
}
