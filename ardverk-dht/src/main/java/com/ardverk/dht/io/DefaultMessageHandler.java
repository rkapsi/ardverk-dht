package com.ardverk.dht.io;

import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;

public class DefaultMessageHandler implements MessageCallback {

    private final RouteTable routeTable;
    
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
    }
    
    public void handleRequest(RequestMessage request) throws Exception {
        
    }
    
    @Override
    public void handleResponse(ResponseMessage response) throws Exception {
    }
    
    @Override
    public void handleTimeout(RequestMessage request) throws Exception {
    }
}
