/*
 * Copyright 2009-2012 Roger Kapsi
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

import javax.inject.Provider;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.dht.KUID;
import org.ardverk.dht.config.PingConfig;
import org.ardverk.dht.entity.PingEntity;
import org.ardverk.dht.message.MessageFactory;
import org.ardverk.dht.message.MessageType;
import org.ardverk.dht.message.PingRequest;
import org.ardverk.dht.message.PingResponse;
import org.ardverk.dht.message.ResponseMessage;
import org.ardverk.dht.routing.Contact;


/**
 * The {@link PingResponseHandler} manages the {@link MessageType#PING} process.
 */
public class PingResponseHandler extends AbstractResponseHandler<PingEntity> {
    
    private final PingConfig config;
    
    private final PingSender sender;
    
    public PingResponseHandler(Provider<MessageDispatcher> messageDispatcher, 
            SocketAddress address, PingConfig config) {
        super(messageDispatcher);
        
        sender = new SocketAddressPingSender(address);
        this.config = config;
    }
    
    public PingResponseHandler(Provider<MessageDispatcher> messageDispatcher, 
            Contact contact, PingConfig config) {
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
        setValue(new PingEntity((PingResponse)response, time, unit));
    }
    
    @Override
    protected void processTimeout(RequestEntity entity, 
            long time, TimeUnit unit) throws IOException {
        setException(new PingTimeoutException(entity, time, unit));
    }
    
    /**
     * An interface that hides the complexity of sending a PING.
     */
    private interface PingSender {
        public void ping() throws IOException;
    }
    
    /**
     * The {@link SocketAddressPingSender} sends a PING to a {@link SocketAddress}.
     */
    private class SocketAddressPingSender implements PingSender {
        
        private final KUID contactId;
        
        private final SocketAddress address;
        
        public SocketAddressPingSender(SocketAddress address) {
            this(null, address);
        }
        
        public SocketAddressPingSender(KUID contactId, 
                SocketAddress address) {
            this.contactId = contactId;
            this.address = address;
        }
    
        @Override
        public void ping() throws IOException {
            MessageFactory factory = getMessageFactory();
            PingRequest request = factory.createPingRequest(address);
            
            long timeout = config.getPingTimeout(TimeUnit.MILLISECONDS);
            send(contactId, request, timeout, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * The {@link ContactPingSender} sends a PING to a {@link Contact}.
     */
    private class ContactPingSender implements PingSender {
        
        private final Contact contact;
        
        public ContactPingSender(Contact contact) {
            this.contact = contact;
        }
        
        @Override
        public void ping() throws IOException {
            MessageFactory factory = getMessageFactory();
            PingRequest request = factory.createPingRequest(contact);
            
            long timeout = config.getPingTimeoutInMillis();
            long adaptiveTimeout = config.getAdaptiveTimeout(
                    contact, timeout, TimeUnit.MILLISECONDS);
            send(contact, request, adaptiveTimeout, TimeUnit.MILLISECONDS);
        }
    }
}