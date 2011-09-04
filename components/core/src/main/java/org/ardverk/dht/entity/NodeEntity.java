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
import org.ardverk.dht.routing.Contact2;

/**
 * The result of a {@link MessageType#FIND_NODE} operation.
 */
public interface NodeEntity extends LookupEntity {
    
    /**
     * Returns number of hops it took to find the k-closest {@link Contact}s.
     */
    public int getHop();
    
    /**
     * Returns the k-closest {@link Contact}s that were found.
     */
    public Contact2[] getClosest();
    
    /**
     * Returns all {@link Contact}s that were found.
     */
    public Contact2[] getContacts();
}