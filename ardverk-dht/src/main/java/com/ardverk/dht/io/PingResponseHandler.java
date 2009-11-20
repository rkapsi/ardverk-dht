package com.ardverk.dht.io;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.Pong;
import com.ardverk.dht.message.PingResponse;

public class PingResponseHandler extends ResponseHandler<Pong, PingResponse> {

    @Override
    protected void innerStart(AsyncFuture<Pong> future)
            throws Exception {   
    }
    
    @Override
    public void handleMessage(PingResponse message) throws Exception {
    }
}
