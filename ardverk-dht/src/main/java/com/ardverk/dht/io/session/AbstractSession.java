package com.ardverk.dht.io.session;

public abstract class AbstractSession implements Session {

    @Override
    public String toString() {
        return getRemoteAddress().toString();
    }
}
