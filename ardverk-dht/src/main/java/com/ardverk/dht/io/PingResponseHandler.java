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

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.lang.Arguments;
import org.ardverk.net.NetworkUtils;

import com.ardverk.dht.KUID;
import com.ardverk.dht.config.PingConfig;
import com.ardverk.dht.entity.DefaultPingEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.PingRequest;
import com.ardverk.dht.message.PingResponse;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.IContact;

public class PingResponseHandler extends AbstractResponseHandler<PingEntity> {
    
    private final PingConfig config;
    
    private final PingSender sender;
    
    public PingResponseHandler(MessageDispatcher messageDispatcher, 
            SocketAddress address, PingConfig config) {
        super(messageDispatcher);
        
        sender = new SocketAddressPingSender(address);
        this.config = config;
    }
    
    public PingResponseHandler(MessageDispatcher messageDispatcher, 
            IContact contact, PingConfig config) {
        super(messageDispatcher);
        
        sender = new ContactPingSender(contact);
        this.config = config;
    }
    
    @Override
    protected void go(AsyncFuture<PingEntity> future) throws IOException {
        sender.ping();
    }
    
    @Override
    protected void processResponse(RequestEntity entity, 
            ResponseMessage response, long time, TimeUnit unit) {
        setValue(new DefaultPingEntity((PingResponse)response, time, unit));
    }
    
    @Override
    protected void processTimeout(RequestEntity entity, 
            long time, TimeUnit unit) throws IOException {
        setException(new PingTimeoutException(entity, time, unit));
    }
    
    private interface PingSender {
        public void ping() throws IOException;
    }
    
    private class SocketAddressPingSender implements PingSender {
        
        private final KUID contactId;
        
        private final SocketAddress address;
        
        public SocketAddressPingSender(SocketAddress address) {
            this(null, address);
        }
        
        public SocketAddressPingSender(KUID contactId, 
                SocketAddress address) {
            if (!NetworkUtils.isValidPort(address)) {
                throw new IllegalArgumentException("address=" + address);
            }
            
            this.contactId = contactId;
            this.address = address;
        }
    
        @Override
        public void ping() throws IOException {
            MessageFactory factory = messageDispatcher.getMessageFactory();
            PingRequest request = factory.createPingRequest(address);
            
            long timeout = config.getPingTimeout(TimeUnit.MILLISECONDS);
            send(contactId, request, timeout, TimeUnit.MILLISECONDS);
        }
    }
    
    private class ContactPingSender implements PingSender {
        
        private final IContact contact;
        
        public ContactPingSender(IContact contact) {
            this.contact = Arguments.notNull(contact, "contact");
        }
        
        @Override
        public void ping() throws IOException {
            MessageFactory factory = messageDispatcher.getMessageFactory();
            PingRequest request = factory.createPingRequest(contact);
            
            long timeout = config.getPingTimeoutInMillis();
            long adaptiveTimeout = config.getAdaptiveTimeout(
                    contact, timeout, TimeUnit.MILLISECONDS);
            send(contact, request, adaptiveTimeout, TimeUnit.MILLISECONDS);
        }
    }
}