package com.ardverk.dht.io;

import com.ardverk.concurrent.AsyncFuture;
import com.ardverk.dht.Pong;
import com.ardverk.dht.message.PingResponse;

public class PingResponseHandler extends MessageHandler<Pong, PingResponse> {

    @Override
    protected void innerStart(AsyncFuture<Pong> future)
            throws Exception {   
    }
    
    @Override
    public void handleMessage(PingResponse message) throws Exception {
    }
}
