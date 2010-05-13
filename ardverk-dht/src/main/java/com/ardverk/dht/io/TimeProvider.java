package com.ardverk.dht.io;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.message.MessageId;

/**
 * 
 */
public interface TimeProvider {

    /**
     * 
     */
    public long getTime(MessageId messageId, TimeUnit unit);
}
