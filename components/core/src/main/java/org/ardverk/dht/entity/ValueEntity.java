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
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.storage.Resource;
import org.ardverk.dht.storage.Value;

/**
 * The result of a {@link MessageType#FIND_VALUE} operation.
 */
public interface ValueEntity extends LookupEntity {
    
    /**
     * Returns the sender who sent us the {@link Resource}.
     * 
     * @see #getResource()
     * @see Resource#getSender()
     */
    public Contact getSender();
    
    /**
     * Returns the creator who created the {@link Resource}.
     * 
     * @see #getResource()
     * @see Resource#getCreator()
     */
    public Contact getCreator();
    
    /**
     * Returns first {@link Resource}'s {@link Value}.
     * 
     * @see #getResource()
     * @see Resource#getValue()
     */
    public Value getValue();
    
    /**
     * Returns the first {@link Resource}.
     * 
     * @see #getResources()
     */
    public Resource getResource();
    
    /**
     * Returns all {@link Resource}s.
     */
    public Resource[] getResources();
}