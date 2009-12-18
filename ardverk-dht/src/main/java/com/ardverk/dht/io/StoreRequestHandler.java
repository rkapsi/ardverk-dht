package com.ardverk.dht.io;

import java.io.IOException;

import com.ardverk.dht.KUID;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.message.StoreRequest;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;

public class StoreRequestHandler extends RequestHandler {

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
        
        KUID key = request.getKey();
        byte[] value = request.getValue();
        
        //System.out.println("STORE: " + routeTable.getLocalhost().getContactId() + " > " + key + " = " + new String(value));

        database.put(key, value);
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        ResponseMessage response = factory.createStoreResponse(request);
        messageDispatcher.send(response);
    }
}
