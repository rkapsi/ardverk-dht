/*
 * Copyright 2009-2011 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardverk.dht.io.transport.Endpoint;
import org.ardverk.dht.message.Message;
import org.ardverk.dht.message.MessageFactory;
import org.ardverk.dht.message.NodeRequest;
import org.ardverk.dht.message.PingRequest;
import org.ardverk.dht.message.RequestMessage;
import org.ardverk.dht.message.ResponseMessage;
import org.ardverk.dht.message.StoreRequest;
import org.ardverk.dht.message.ValueRequest;
import org.ardverk.dht.routing.RouteTable;
import org.ardverk.dht.storage.Database;
import org.ardverk.dht.storage.StoreForward;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A default implementation of {@link MessageDispatcher}.
 */
public class DefaultMessageDispatcher extends MessageDispatcher {

    private static final Logger LOG 
        = LoggerFactory.getLogger(DefaultMessageDispatcher.class);
    
    private final DefaultMessageHandler defaultHandler;
    
    private final PingRequestHandler ping;
    
    private final NodeRequestHandler node;
    
    private final ValueRequestHandler value;
    
    private final StoreRequestHandler store;
    
    public DefaultMessageDispatcher(MessageFactory factory, 
            StoreForward storeForward, RouteTable routeTable, 
            Database database) {
        super(factory);
        
        defaultHandler = new DefaultMessageHandler(storeForward, routeTable);
        ping = new PingRequestHandler(this);
        node = new NodeRequestHandler(this, routeTable);
        value = new ValueRequestHandler(this, routeTable, database);
        store = new StoreRequestHandler(this, routeTable, database);
    }
    
    /**
     * Returns the {@link DefaultMessageHandler} which is called for 
     * every incoming {@link Message}.
     */
    public DefaultMessageHandler getDefaultHandler() {
        return defaultHandler;
    }
    
    /**
     * Returns the {@link PingRequestHandler} which is called for
     * every incoming {@link PingRequest}.
     */
    public PingRequestHandler getPingRequestHandler() {
        return ping;
    }

    /**
     * Returns the {@link NodeRequestHandler} which is called for every
     * incoming {@link NodeRequest}.
     */
    public NodeRequestHandler getNodeRequestHandler() {
        return node;
    }
    
    /**
     * Returns the {@link ValueRequestHandler} which is called for every
     * incoming {@link ValueRequest}.
     */
    public ValueRequestHandler getValueRequestHandler() {
        return value;
    }

    /**
     * Returns the {@link StoreRequestHandler} which is called for every
     * incoming {@link StoreRequest}.
     */
    public StoreRequestHandler getStoreRequestHandler() {
        return store;
    }

    @Override
    protected void handleRequest(Endpoint endpoint, 
            RequestMessage request) throws IOException {
        
        defaultHandler.handleRequest(request);
        
        if (request instanceof PingRequest) {
            ping.handleRequest(endpoint, request);
        } else if (request instanceof NodeRequest) {
            node.handleRequest(endpoint, request);
        } else if (request instanceof ValueRequest) {
            value.handleRequest(endpoint, request);
        } else if (request instanceof StoreRequest) {
            store.handleRequest(endpoint, request);
        } else {
            unhandledRequest(endpoint, request);
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

    protected void unhandledRequest(Endpoint endpoint, 
            RequestMessage message) throws IOException {
        if (LOG.isErrorEnabled()) {
            LOG.error("Unhandled Request: " + message);
        }
    }
}