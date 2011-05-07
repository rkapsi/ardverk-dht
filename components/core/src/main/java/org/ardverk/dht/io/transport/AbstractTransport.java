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

package org.ardverk.dht.io.transport;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.ardverk.dht.KUID;
import org.ardverk.dht.message.Message;
import org.ardverk.dht.message.RequestMessage;
import org.ardverk.dht.message.ResponseMessage;

/**
 * An abstract implementation of {@link Transport}.
 */
public abstract class AbstractTransport implements Transport {
    
    private final AtomicReference<TransportCallback> callbackRef 
        = new AtomicReference<TransportCallback>();
    
    @Override
    public void bind(TransportCallback callback) throws IOException {
        if (callback == null) {
            throw new NullPointerException("callback");
        }
        
        if (!callbackRef.compareAndSet(null, callback)) {
            throw new IOException();
        }
    }
    
    @Override
    public void unbind() {
        callbackRef.set(null);
    }

    @Override
    public boolean isBound() {
        return callbackRef.get() != null;
    }
    
    /**
     * A helper method to notify the {@link TransportCallback.Inbound} callback.
     */
    protected boolean handleResponse(ResponseMessage response) throws IOException {
        TransportCallback callback = callbackRef.get();
        if (callback != null) {
            return callback.handleResponse(response);
        }
        return false;
    }
    
    /**
     * A helper method to notify the {@link TransportCallback.Inbound} callback.
     */
    protected ResponseMessage handleRequest(RequestMessage request) throws IOException {
        TransportCallback callback = callbackRef.get();
        if (callback != null) {
            return callback.handleRequest(request);
        }
        
        throw new IOException();
    }
    
    protected boolean messageSent(RequestMessage request, 
            ResponseMessage response) {
        KUID contactId = request.getContact().getId();
        return messageSent(contactId, response);
    }
    
    /**
     * A helper method to notify the {@link TransportCallback.Outbound} callback.
     */
    protected boolean messageSent(KUID contactId, Message message) {
        TransportCallback callback = callbackRef.get();
        if (callback != null) {
            callback.messageSent(contactId, message);
            return true;
        }
        return false;
    }
    
    protected boolean handleException(Message message, Throwable t) {
        return handleException(this, message, t);
    }
    
    /**
     * A helper method to notify the {@link TransportCallback.Outbound} callback.
     */
    protected boolean handleException(Endpoint endpoint, 
            Message message, Throwable t) {
        
        TransportCallback callback = callbackRef.get();
        if (callback != null) {
            callback.handleException(endpoint, message, t);
            return true;
        }
        return false;
    }
}