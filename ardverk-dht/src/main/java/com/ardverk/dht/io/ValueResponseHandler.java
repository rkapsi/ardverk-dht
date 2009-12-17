package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.RouteTable;

public class ValueResponseHandler extends ResponseHandler<ValueEntity> {

    private final NodeResponseHandler bla;
    
    public ValueResponseHandler(
            MessageDispatcher messageDispatcher, 
            RouteTable routeTable, KUID key) {
        super(messageDispatcher);
        
        bla = new NodeResponseHandler(messageDispatcher, routeTable, key);
    }

    @Override
    protected void go(AsyncFuture<ValueEntity> future) throws Exception {
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
