package com.ardverk.dht.io;

import com.ardverk.dht.message.Message;

public interface MessageCallback {

    public void handleMessage(Message message) throws Exception;
}
