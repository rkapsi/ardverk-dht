package com.ardverk.dht.io;

import com.ardverk.dht.message.Message;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;

public class DefaultMessageHandler implements MessageHandler<Message> {

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
    
    @Override
    public void handleMessage(Message message) {
        if (message instanceof RequestMessage) {
            handleRequest((RequestMessage)message);
        } else {
            handleResponse((ResponseMessage)message);
        }
    }
    
    private void handleRequest(RequestMessage message) {
        
    }
    
    private void handleResponse(ResponseMessage message) {
        
    }
}
