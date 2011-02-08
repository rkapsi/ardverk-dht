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

import org.ardverk.dht.message.MessageType;
import org.ardverk.dht.message.StoreResponse;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.storage.ValueTuple;

/**
 * The result of a {@link MessageType#STORE} operation.
 */
public interface StoreEntity extends Entity {
    
    /**
     * Returns all {@link Contact}s along the store path.
     */
    public Contact[] getContacts();
    
    /**
     * Returns the {@link ValueTuple} that was stored.
     */
    public ValueTuple getValueTuple();
    
    /**
     * Returns the {@link Contact}s where we attempted to store
     * a value and received responses from.
     * 
     * @see #getStoreResponses()
     */
    public Contact[] getStoreContacts();
    
    /**
     * Returns all {@link StoreResponse}s.
     */
    public StoreResponse[] getStoreResponses();
    
    /**
     * Returns {@code true} if all {@link StoreResponse}s indicate
     * that they were successful.
     * 
     * @see #getStoreResponses()
     */
    public boolean isSuccess();
}