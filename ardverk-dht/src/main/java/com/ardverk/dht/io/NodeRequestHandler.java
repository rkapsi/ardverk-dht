package com.ardverk.dht.io;

import java.io.IOException;

import org.ardverk.lang.NullArgumentException;

import com.ardverk.dht.KUID;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.NodeRequest;
import com.ardverk.dht.message.NodeResponse;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.routing.Contact2;
import com.ardverk.dht.routing.RouteTable;

public class NodeRequestHandler extends AbstractRequestHandler {

    private final RouteTable routeTable;
    
    public NodeRequestHandler(
            MessageDispatcher messageDispatcher, 
            RouteTable routeTable) {
        super(messageDispatcher);
        
        if (routeTable == null) {
            throw new NullArgumentException("routeTable");
        }
        
        this.routeTable = routeTable;
    }

    @Override
    public void handleRequest(RequestMessage message) throws IOException {
        
        //System.out.println("REQUEST: " + message);
        
        NodeRequest request = (NodeRequest)message;
        KUID key = request.getKey();
        
        Contact2[] contacts = routeTable.select(key);
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        NodeResponse response = factory.createNodeResponse(request, contacts);
        send(request, response);
    }
}
