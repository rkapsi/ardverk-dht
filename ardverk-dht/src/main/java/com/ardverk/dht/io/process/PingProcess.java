package com.ardverk.dht.io.process;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.message.PingResponse;

public class PingProcess implements AsyncProcess<PingResponse> {

    @Override
    public void start(AsyncFuture<PingResponse> future) throws Exception {
    }
}
