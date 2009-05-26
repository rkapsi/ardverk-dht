package com.ardverk.dht.io;

import com.ardverk.dht.message.Message;

public interface MessageHandler<T extends Message> {

    public void handleMessage(T message) throws Exception;
}
