package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;

public class DefaultMessageHandler implements MessageCallback {

    /*private final RouteTable routeTable;
    
    private final Database database;
    
    public DefaultMessageHandler(RouteTable routeTable, Database database) {
        if (routeTable == null) {
            throw new NullPointerException("routeTable");
        }
        
        if (database == null) {
            throw new NullPointerException("database");
        }
        
        this.routeTable = routeTable;
        this.database = database;
    }*/
    
    public void handleRequest(RequestMessage request) throws IOException {
        
    }
    
    @Override
    public void handleResponse(RequestMessage request, 
            ResponseMessage response, long time, TimeUnit unit) throws IOException {
    }
    
    public void handleLateResponse(ResponseMessage message) throws IOException {
        
    }
    
    @Override
    public void handleTimeout(RequestMessage request, 
            long time, TimeUnit unit) throws IOException {
    }
}
