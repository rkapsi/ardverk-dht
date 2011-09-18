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

import org.ardverk.dht.message.StoreResponse;
import org.ardverk.dht.routing.Contact;

/**
 * A default implementation of {@link PutEntity}.
 */
public class PutEntity extends LookupEntity {

    private final NodeEntity nodeEntity;
    
    private final StoreEntity storeEntity;
    
    public PutEntity(NodeEntity nodeEntity, StoreEntity storeEntity) {
        super(nodeEntity.getId(), 
                EntityUtils.getTimeInMillis(nodeEntity, storeEntity), 
                TimeUnit.MILLISECONDS);
        
        this.nodeEntity = nodeEntity;
        this.storeEntity = storeEntity;
    }

    public NodeEntity getNodeEntity() {
        return nodeEntity;
    }
    
    public StoreEntity getStoreEntity() {
        return storeEntity;
    }

    public StoreResponse[] getStoreResponses() {
        return storeEntity.getStoreResponses();
    }
    
    public Contact[] getStoreContacts() {
        return storeEntity.getStoreContacts();
    }
}