package com.ardverk.dht.io;

import java.io.IOException;

import com.ardverk.dht.KUID;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.message.StoreRequest;
import com.ardverk.dht.routing.Contact2;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;
import com.ardverk.dht.storage.Database.Condition;

public class StoreRequestHandler extends AbstractRequestHandler {

    private final RouteTable routeTable;
    
    private final Database database;
    
    public StoreRequestHandler(
            MessageDispatcher messageDispatcher,
            RouteTable routeTable, 
            Database database) {
        super(messageDispatcher);
        
        if (routeTable == null) {
            throw new NullPointerException("routeTable");
        }
        
        if (database == null) {
            throw new NullPointerException("database");
        }
        
        this.routeTable = routeTable;
        this.database = database;
    }

    @Override
    public void handleRequest(RequestMessage message) throws IOException {
        
        StoreRequest request = (StoreRequest)message;
        
        Contact2 src = message.getContact();
        KUID key = request.getKey();
        byte[] value = request.getValue();
        
        Condition status = database.store(src, key, value);
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        ResponseMessage response = factory.createStoreResponse(request, status);
        send(request, response);
    }
}
