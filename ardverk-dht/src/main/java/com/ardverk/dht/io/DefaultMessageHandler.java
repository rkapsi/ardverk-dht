/*
 * Copyright 2009-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ardverk.dht.io;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.KUID;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RoundTripTime;
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
        
        if (src instanceof RoundTripTime) {
            ((RoundTripTime)src).setRoundTripTime(time, unit);
        }
        
        storeForward.handleResponse(src);
        routeTable.add(src);
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