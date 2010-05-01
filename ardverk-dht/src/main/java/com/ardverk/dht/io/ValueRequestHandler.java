package com.ardverk.dht.io;

import java.io.IOException;

import org.ardverk.lang.NullArgumentException;

import com.ardverk.dht.KUID;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.message.ValueRequest;
import com.ardverk.dht.routing.Contact2;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;

public class ValueRequestHandler extends AbstractRequestHandler {

    private final RouteTable routeTable;
    
    private final Database database;
    
    public ValueRequestHandler(
            MessageDispatcher messageDispatcher, 
            RouteTable routeTable, 
            Database database) {
        super(messageDispatcher);
        
        if (routeTable == null) {
            throw new NullArgumentException("routeTable");
        }
        
        if (database == null) {
            throw new NullArgumentException("database");
        }
        
        this.routeTable = routeTable;
        this.database = database;
    }

    @Override
    public void handleRequest(RequestMessage message) throws IOException {
        ValueRequest request = (ValueRequest)message;
        
        KUID key = request.getKey();
        byte[] value = database.lookup(key);
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        ResponseMessage response = null;
        
        if (value != null) {
            response = factory.createValueResponse(request, value);
        } else {
            Contact2[] contacts = routeTable.select(key);
            response = factory.createNodeResponse(request, contacts);
        }
        
        send(request, response);
    }
}
