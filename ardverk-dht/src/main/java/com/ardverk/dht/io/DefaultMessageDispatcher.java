package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.ardverk.dht.codec.MessageCodec;
import com.ardverk.dht.logging.LoggerUtils;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.NodeRequest;
import com.ardverk.dht.message.PingRequest;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.message.StoreRequest;
import com.ardverk.dht.message.ValueRequest;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;
import com.ardverk.dht.storage.StoreForward;

public class DefaultMessageDispatcher extends MessageDispatcher {

    private static final Logger LOG 
        = LoggerUtils.getLogger(DefaultMessageDispatcher.class);
    
    private final DefaultMessageHandler defaultHandler;
    
    private final PingRequestHandler ping;
    
    private final NodeRequestHandler node;
    
    private final ValueRequestHandler value;
    
    private final StoreRequestHandler store;
    
    public DefaultMessageDispatcher(MessageFactory factory, 
            MessageCodec codec, StoreForward storeForward, 
            RouteTable routeTable, Database database) {
        super(factory, codec);
        
        defaultHandler = new DefaultMessageHandler(storeForward, routeTable);
        ping = new PingRequestHandler(this);
        node = new NodeRequestHandler(this, routeTable);
        value = new ValueRequestHandler(this, routeTable, database);
        store = new StoreRequestHandler(this, database);
    }
    
    public DefaultMessageHandler getDefaultHandler() {
        return defaultHandler;
    }
    
    public PingRequestHandler getPingRequestHandler() {
        return ping;
    }

    public NodeRequestHandler getNodeRequestHandler() {
        return node;
    }
    
    public ValueRequestHandler getValueRequestHandler() {
        return value;
    }

    public StoreRequestHandler getStoreRequestHandler() {
        return store;
    }

    @Override
    protected void handleRequest(RequestMessage request) throws IOException {
        
        defaultHandler.handleRequest(request);
        
        if (request instanceof PingRequest) {
            ping.handleRequest(request);
        } else if (request instanceof NodeRequest) {
            node.handleRequest(request);
        } else if (request instanceof ValueRequest) {
            value.handleRequest(request);
        } else if (request instanceof StoreRequest) {
            store.handleRequest(request);
        } else {
            unhandledRequest(request);
        }
    }
    
    @Override
    protected void handleResponse(MessageCallback callback,
            RequestEntity entity, ResponseMessage response, long time,
            TimeUnit unit) throws IOException {
        
        super.handleResponse(callback, entity, response, time, unit);
        defaultHandler.handleResponse(entity, response, time, unit);
    }

    @Override
    protected void handleTimeout(MessageCallback callback,
            RequestEntity entity, long time, TimeUnit unit)
            throws IOException {
        
        super.handleTimeout(callback, entity, time, unit);
        defaultHandler.handleTimeout(entity, time, unit);
    }

    @Override
    protected void lateResponse(ResponseMessage response) throws IOException {
        defaultHandler.handleLateResponse(response);
    }

    protected void unhandledRequest(RequestMessage message) throws IOException {
        if (LOG.isErrorEnabled()) {
            LOG.error("Unhandled Request: " + message);
        }
    }
}
