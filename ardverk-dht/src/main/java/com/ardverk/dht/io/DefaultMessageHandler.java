package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;

public class DefaultMessageHandler implements MessageCallback {

    private final MessageDispatcher messageDispatcher;
    
    private final RouteTable routeTable;
    
    public DefaultMessageHandler(MessageDispatcher messageDispatcher, 
            RouteTable routeTable) {
        
        if (messageDispatcher == null) {
            throw new NullPointerException("messageDispatcher");
        }
        
        if (routeTable == null) {
            throw new NullPointerException("routeTable");
        }
        
        this.messageDispatcher = messageDispatcher;
        this.routeTable = routeTable;
    }
    
    public void handleRequest(RequestMessage request) throws IOException {
        Contact src = request.getContact();
        routeTable.add(src);
    }
    
    @Override
    public void handleResponse(RequestMessage request, 
            ResponseMessage response, long time, TimeUnit unit) throws IOException {
        
        Contact src = response.getContact();
        routeTable.add(src);
    }
    
    public void handleLateResponse(ResponseMessage response) throws IOException {
        Contact src = response.getContact();
        routeTable.add(src);
    }
    
    @Override
    public void handleTimeout(RequestMessage request, 
            long time, TimeUnit unit) throws IOException {
        
        Contact dst = request.getContact();
        routeTable.failure(dst.getContactId(), 
                dst.getRemoteAddress());
    }

    @Override
    public void handleException(RequestMessage request, Throwable exception) {
        // Do nothing!
    }
}
