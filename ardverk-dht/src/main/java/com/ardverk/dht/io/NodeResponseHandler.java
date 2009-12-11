package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;

public class NodeResponseHandler extends ResponseHandler<NodeEntity> {
    
    public NodeResponseHandler(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }

    @Override
    protected void go(AsyncFuture<NodeEntity> future)
            throws Exception {
    }

    @Override
    protected void processResponse(RequestMessage request,
            ResponseMessage response, long time, TimeUnit unit)
            throws IOException {
    }

    @Override
    protected void processTimeout(RequestMessage request, long time,
            TimeUnit unit) throws IOException {
    }
}
