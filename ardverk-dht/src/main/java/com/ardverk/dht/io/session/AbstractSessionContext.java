package com.ardverk.dht.io.session;

public abstract class AbstractSessionContext implements SessionContext {

    @Override
    public String toString() {
        return "remote=" + getRemoteAddress().toString();
    }
}
