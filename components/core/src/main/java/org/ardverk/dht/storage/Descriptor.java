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

import org.ardverk.dht.KUID;
import org.ardverk.dht.lang.Identifier;
import org.ardverk.dht.routing.Contact;
import org.ardverk.lang.Age;
import org.ardverk.lang.Epoch;
import org.ardverk.version.VectorClock;

/**
 * A {@link Descriptor} describes a {@link Value}'s {@link Resource}, its 
 * creator and sender (possibly the same), its creation time (local time)
 * and its age.
 */
public interface Descriptor extends Identifier, Epoch, Age {

    /**
     * Returns the sender of the {@link ValueTuple}.
     */
    public Contact getSender();
    
    /**
     * Returns the creator of the {@link ValueTuple}.
     */
    public Contact getCreator();
    
    /**
     * Returns the {@link Resource} of the {@link ValueTuple}.
     */
    public Resource getResource();
    
    /**
     * Returns the {@link Value}'s {@link VectorClock}.
     */
    public VectorClock<KUID> getVectorClock();
}
