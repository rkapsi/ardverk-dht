package com.ardverk.dht.io;

import java.io.IOException;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.message.StoreRequest;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;
import com.ardverk.dht.storage.Database.Condition;
import com.ardverk.dht.storage.ValueTuple;

public class StoreRequestHandler extends AbstractRequestHandler {

    private final RouteTable routeTable;
    
    private final Database database;
    
    public StoreRequestHandler(
            MessageDispatcher messageDispatcher,
            RouteTable routeTable, 
            Database database) {
        super(messageDispatcher);
        
        this.routeTable = Arguments.notNull(routeTable, "routeTable");
        this.database = Arguments.notNull(database, "database");
    }

    @Override
    public void handleRequest(RequestMessage message) throws IOException {
        
        StoreRequest request = (StoreRequest)message;
        
        ValueTuple tuple = request.getValueTuple();
        Condition condition = database.store(tuple);
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        ResponseMessage response = factory.createStoreResponse(request, condition);
        send(request, response);
    }
}
