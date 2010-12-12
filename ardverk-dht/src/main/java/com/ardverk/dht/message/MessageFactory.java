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

package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.Database.Condition;
import com.ardverk.dht.storage.ValueTuple;

/**
 * 
 */
public interface MessageFactory {
    
    /**
     * 
     */
    public MessageId createMessageId(SocketAddress dst);
    
    /**
     * 
     */
    public boolean isFor(MessageId messageId, SocketAddress src);
    
    /**
     * 
     */
    public PingRequest createPingRequest(SocketAddress dst);
    
    /**
     * 
     */
    public PingRequest createPingRequest(Contact dst);
    
    /**
     * 
     */
    public PingResponse createPingResponse(PingRequest request);
    
    /**
     * 
     */
    public NodeRequest createNodeRequest(Contact dst, KUID key);
    
    /**
     * 
     */
    public NodeResponse createNodeResponse(LookupRequest request, Contact[] contacts);
    
    /**
     * 
     */
    public ValueRequest createValueRequest(Contact dst, KUID key);
    
    /**
     * 
     */
    public ValueResponse createValueResponse(LookupRequest request, ValueTuple tuple);
    
    /**
     * 
     */
    public StoreRequest createStoreRequest(Contact dst, ValueTuple tuple);
    
    /**
     * 
     */
    public StoreResponse createStoreResponse(StoreRequest request, Condition status);
}