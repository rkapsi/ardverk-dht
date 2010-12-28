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

package com.ardverk.dht.storage;

import com.ardverk.dht.lang.Age;
import com.ardverk.dht.lang.Epoch;
import com.ardverk.dht.lang.Identifier;
import com.ardverk.dht.routing.Contact;

/**
 * A {@link ValueTuple} is at its core a simple key-value pair
 * that holds additional information such as the creator and 
 * sender of the value, when it was created and what it's current
 * age is.
 */
public interface ValueTuple extends Identifier, Epoch, Age {
    
    /**
     * Returns the sender of the {@link ValueTuple}.
     */
    public Contact getSender();
    
    /**
     * Returns the creator of the {@link ValueTuple}.
     */
    public Contact getCreator();
    
    /**
     * Returns the value of the {@link ValueTuple}.
     */
    public byte[] getValue();
    
    /**
     * Returns the size of the value.
     * 
     * @see #getValue()
     */
    public int size();
    
    /**
     * Returns {@code true} if the value is empty.
     */
    public boolean isEmpty();
}