package com.ardverk.dht.io;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.KUID;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.StoreForward;

public class DefaultMessageHandler implements MessageCallback {

    private final StoreForward storeForward;
    
    private final RouteTable routeTable;
    
    public DefaultMessageHandler(StoreForward storeForward, 
            RouteTable routeTable) {
        this.storeForward = Arguments.notNull(storeForward, "storeForward");
        this.routeTable = Arguments.notNull(routeTable, "routeTable");
    }
    
    public void handleRequest(RequestMessage request) throws IOException {
        Contact src = request.getContact();
        storeForward.handleRequest(src);
        routeTable.add(src);
    }
    
    @Override
    public void handleResponse(RequestEntity entity, 
            ResponseMessage response, long time, TimeUnit unit) throws IOException {
        
        Contact src = response.getContact();
        Contact rtt = src.setRoundTripTime(time, unit);
        storeForward.handleResponse(rtt);
        routeTable.add(rtt);
    }
    
    public void handleLateResponse(ResponseMessage response) throws IOException {
        Contact src = response.getContact();
        storeForward.handleLateResponse(src);
        routeTable.add(src);
    }
    
    @Override
    public void handleTimeout(RequestEntity entity, 
            long time, TimeUnit unit) throws IOException {
        
        KUID contactId = entity.getContactId();
        SocketAddress address = entity.getAddress();
        
        routeTable.handleIoError(contactId, address);
    }

    @Override
    public void handleException(RequestEntity entity, Throwable exception) {
        // Do nothing!
    }
}
