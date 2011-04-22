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

package org.ardverk.dht.storage;


/**
 * A {@link ValueTuple} is at its core a simple key-value pair
 * that holds additional information such as the creator and 
 * sender of the value, when it was created and what it's current
 * age is.
 */
public interface ValueTuple {
    
    /**
     * Returns the {@link Descriptor} of the {@link ValueTuple}.
     */
    public Descriptor getDescriptor();
    
    /**
     * Returns the {@link Value} of the {@link ValueTuple}.
     */
    public Value getValue();
}