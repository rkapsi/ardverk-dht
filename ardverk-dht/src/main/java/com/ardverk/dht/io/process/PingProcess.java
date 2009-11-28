package com.ardverk.dht.io.process;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.io.MessageDispatcher;
import com.ardverk.dht.message.PingResponse;

public class PingProcess extends AbstractProcess<PingResponse> {

    public PingProcess(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }

    @Override
    public void start(AsyncFuture<PingResponse> future) throws Exception {
    }
}
