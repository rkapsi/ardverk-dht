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

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.lang.Identifier;
import com.ardverk.dht.routing.IContact;

public interface ValueTuple extends Identifier {

    /**
     * Returns the {@link ValueTuple}'s creation time.
     */
    public long getCreationTime();
    
    /**
     * Returns the {@link ValueTuple}'s age in the given {@link TimeUnit}.
     */
    public long getAge(TimeUnit unit);
    
    /**
     * Returns the {@link ValueTuple}'s age in milliseconds.
     */
    public long getAgeInMillis();
    
    /**
     * Returns the sender of the {@link ValueTuple}.
     */
    public IContact getSender();
    
    /**
     * Returns the creator of the {@link ValueTuple}.
     */
    public IContact getCreator();
    
    /**
     * Returns the {@link Value} of the {@link ValueTuple}.
     */
    public byte[] getValue();
    
    /**
     * Returns the size of the value.
     * 
     * @see #getValue()
     */
    public int size();
    
    /**
     * Returns {@code true} if the {@link Value} is empty.
     */
    public boolean isEmpty();
}