package com.ardverk.dht.concurrent;

import org.ardverk.concurrent.AsyncProcessFuture;

public interface ArdverkFuture<V> extends AsyncProcessFuture<V> {

    /**
     * Attaches an object to this {@link ArdverkFuture}.
     */
    public void setAttachment(Object attachment);
    
    /**
     * Returns an attached object.
     */
    public Object getAttachment();
}
