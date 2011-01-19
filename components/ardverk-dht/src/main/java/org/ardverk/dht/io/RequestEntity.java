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

package org.ardverk.dht.io;

import java.net.SocketAddress;

import org.ardverk.dht.KUID;
import org.ardverk.dht.lang.Identifier;
import org.ardverk.dht.message.MessageId;
import org.ardverk.dht.message.NodeRequest;
import org.ardverk.dht.message.NodeResponse;
import org.ardverk.dht.message.PingRequest;
import org.ardverk.dht.message.PingResponse;
import org.ardverk.dht.message.RequestMessage;
import org.ardverk.dht.message.ResponseMessage;
import org.ardverk.dht.message.StoreRequest;
import org.ardverk.dht.message.StoreResponse;
import org.ardverk.dht.message.ValueRequest;
import org.ardverk.dht.message.ValueResponse;
import org.ardverk.dht.routing.Contact;


/**
 * The {@link RequestEntity} holds the necessary information to verify and 
 * validate a {@link ResponseMessage} that we're receiving in response for 
 * a {@link RequestMessage}.
 */
public class RequestEntity implements Identifier {

    private final KUID contactId;
    
    private final RequestMessage request;
    
    public RequestEntity(KUID contactId, RequestMessage request) {
        this.contactId = contactId;
        this.request = request;
    }

    @Override
    public KUID getId() {
        return contactId;
    }
    
    /**
     * Returns the remove host's {@link SocketAddress}.
     */
    public SocketAddress getAddress() {
        return request.getAddress();
    }

    /**
     * Returns the {@link RequestMessage} we sent to the remote host.
     */
    public RequestMessage getRequest() {
        return request;
    }
    
    /**
     * Returns the {@link RequestMessage}'s {@link MessageId}.
     * 
     * @see RequestMessage#getMessageId()
     */
    MessageId getMessageId() {
        return request.getMessageId();
    }
    
    /**
     * Checks if the {@link ResponseMessage} of the expected type.
     */
    boolean check(ResponseMessage response) {
        return checkType(response) && checkContactId(response);
    }
    
    /**
     * Checks the {@link Contact}'s {@link KUID}
     */
    private boolean checkContactId(ResponseMessage response) {
        if (contactId == null) {
            return (response instanceof PingResponse);
        }
        
        Contact contact = response.getContact();
        KUID otherId = contact.getId();
        
        return contactId.equals(otherId);
    }
    
    /**
     * Checks the type of the {@link ResponseMessage}.
     */
    private boolean checkType(ResponseMessage response) {
        if (request instanceof PingRequest) {
            return response instanceof PingResponse;
        } else if (request instanceof NodeRequest) {
            return response instanceof NodeResponse;
        } else if (request instanceof ValueRequest) {
            return response instanceof ValueResponse 
                || response instanceof NodeResponse;
        } else if (request instanceof StoreRequest) {
            return response instanceof StoreResponse;
        }
        
        return false;
    }
}