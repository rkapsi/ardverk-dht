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

package org.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import org.ardverk.dht.message.Content;
import org.ardverk.dht.message.StoreResponse;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.storage.ResourceId;
import org.ardverk.dht.storage.Status;

/**
 * A default implementation of {@link StoreEntity}.
 */
public class DefaultStoreEntity extends AbstractEntity implements StoreEntity {

    private final Contact[] contacts;
    
    private final ResourceId resourceId;
    
    private final Content content;
    
    private final StoreResponse[] responses;
    
    public DefaultStoreEntity(Contact[] contacts, ResourceId resourceId, 
            Content content, StoreResponse[] responses, 
            long time, TimeUnit unit) {
        super(time, unit);
        
        this.contacts = contacts;
        this.resourceId = resourceId;
        this.content = content;
        this.responses = responses;
    }
    
    @Override
    public Contact[] getContacts() {
        return contacts;
    }

    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public Content getContent() {
        return content;
    }
    
    @Override
    public Contact[] getStoreContacts() {
        Contact[] contacts = new Contact[responses.length];
        for (int i = 0; i < responses.length; i++) {
            contacts[i] = responses[i].getContact();
        }
        return contacts;
    }

    @Override
    public StoreResponse[] getStoreResponses() {
        return responses;
    }
    
    @Override
    public boolean isSuccess() {
        for (StoreResponse response : responses) {
            Status status = Status.create(response.getContent());
            if (!status.equals(Status.SUCCESS)) {
                return false;
            }
        }
        return true;
    }
}