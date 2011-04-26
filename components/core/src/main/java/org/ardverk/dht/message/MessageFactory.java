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

package org.ardverk.dht.message;

import java.net.SocketAddress;

import org.ardverk.dht.KUID;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.storage.Resource;
import org.ardverk.dht.storage.ResourceId;
import org.ardverk.dht.storage.Status;

/**
 * A factory interface to create various {@link Message}es such as
 * {@link PingRequest} etc.
 */
public interface MessageFactory {
    
    /**
     * Creates and returns a {@link MessageId}.
     */
    public MessageId createMessageId(SocketAddress dst);
    
    /**
     * Returns {@code true} if the given {@link MessageId} is for 
     * the {@link SocketAddress}.
     */
    public boolean isFor(MessageId messageId, SocketAddress src);
    
    /**
     * Creates and returns a {@link PingRequest}.
     */
    public PingRequest createPingRequest(SocketAddress dst);
    
    /**
     * Creates and returns a {@link PingRequest}.
     */
    public PingRequest createPingRequest(Contact dst);
    
    /**
     * Creates and returns a {@link PingResponse}.
     */
    public PingResponse createPingResponse(PingRequest request);
    
    /**
     * Creates and returns a {@link NodeRequest}.
     */
    public NodeRequest createNodeRequest(Contact dst, KUID key);
    
    /**
     * Creates and returns a {@link NodeResponse}.
     */
    public NodeResponse createNodeResponse(LookupRequest request, Contact[] contacts);
    
    /**
     * Creates and returns a {@link ValueRequest}.
     */
    public ValueRequest createValueRequest(Contact dst, ResourceId resourceId);
    
    /**
     * Creates and returns a {@link ValueResponse}.
     */
    public ValueResponse createValueResponse(LookupRequest request, Resource resource);
    
    /**
     * Creates and returns a {@link StoreRequest}.
     */
    public StoreRequest createStoreRequest(Contact dst, 
            ResourceId resourceId, Resource resource);
    
    /**
     * Creates and returns a {@link StoreResponse}.
     */
    public StoreResponse createStoreResponse(StoreRequest request, Status status);
}