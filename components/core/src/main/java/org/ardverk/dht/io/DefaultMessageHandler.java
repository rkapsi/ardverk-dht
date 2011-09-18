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
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ardverk.dht.KUID;
import org.ardverk.dht.message.Message;
import org.ardverk.dht.message.RequestMessage;
import org.ardverk.dht.message.ResponseMessage;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.Identity;
import org.ardverk.dht.routing.RoundTripTime;
import org.ardverk.dht.routing.RouteTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The {@link DefaultMessageHandler} is called for every {@link Message}
 * we're receiving. It's purpose is to add the remote {@link Contact}
 * to the local {@link RouteTable} and maybe trigger the store-forwarding.
 */
@Singleton
public class DefaultMessageHandler implements MessageCallback {

    private static final Logger LOG 
        = LoggerFactory.getLogger(DefaultMessageHandler.class);
    
    private final RouteTable routeTable;
    
    @Inject
    public DefaultMessageHandler(RouteTable routeTable) {
        this.routeTable = routeTable;
    }
    
    public void handleRequest(RequestMessage request) throws IOException {
        Contact src = request.getContact();
        routeTable.add(src);
    }
    
    @Override
    public boolean handleResponse(RequestEntity entity, 
            ResponseMessage response, long time, TimeUnit unit) throws IOException {
        
        Contact src = response.getContact();
        
        if (src instanceof RoundTripTime) {
            ((RoundTripTime)src).setRoundTripTime(time, unit);
        }
        
        SocketAddress address = response.getAddress();
        updateContactAddress(address);
        
        routeTable.add(src);
        
        return true;
    }
    
    public void handleLateResponse(ResponseMessage response) throws IOException {
        Contact src = response.getContact();
        routeTable.add(src);
    }
    
    @Override
    public void handleTimeout(RequestEntity entity, 
            long time, TimeUnit unit) throws IOException {
        
        if (LOG.isTraceEnabled()) {
            LOG.trace("Timeout: entity=" + entity + ", time=" + time + ", unit=" + unit);
        }
        
        handleIoError(entity);
    }
    
    @Override
    public void handleIllegalResponse(RequestEntity entity,
            ResponseMessage response, long time, TimeUnit unit)
            throws IOException {
        
        // Do nothing!
        
        if (LOG.isErrorEnabled()) {
            LOG.error("Illegal Response: entity=" + entity + ", response=" + response);
        }
        
        handleIoError(entity);
    }

    @Override
    public void handleException(RequestEntity entity, Throwable exception) {
        // Do nothing!
        
        if (LOG.isTraceEnabled()) {
            LOG.trace("Exception: entity=" + entity, exception);
        }
        
        handleIoError(entity);
    }
    
    private void handleIoError(RequestEntity entity) {
        KUID contactId = entity.getId();
        SocketAddress address = entity.getAddress();
        
        routeTable.handleIoError(contactId, address);
    }
    
    /**
     * Each message contains the receiver's (our) {@link SocketAddress}.
     * We're using it to update our {@link Identity}'s contact address.
     */
    private void updateContactAddress(SocketAddress address) {
        if (address != null) {
            Identity localhost = routeTable.getIdentity();
            SocketAddress current = localhost.getSocketAddress();
            
            if (current == null || !current.equals(address)) {
                localhost.setSocketAddress(address);
            }
        }
    }
}