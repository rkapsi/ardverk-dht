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

import org.ardverk.dht.DHT;
import org.ardverk.dht.KUID;
import org.ardverk.dht.routing.Contact;
import org.ardverk.io.Bindable;

/**
 * A simple and minimum interface of a {@link Database} 
 * that is needed by the DHT.
 */
public interface Database extends Bindable<DHT> {
    
    /**
     * Returns the {@link DatabaseConfig}.
     */
    public DatabaseConfig getDatabaseConfig();
    
    /**
     * Stores the given {@link Value} and returns a {@link Status}.
     */
    public Value store(Key key, Value value);
    
    /**
     * Returns a {@link Value} for the given {@link Key}.
     */
    public Value get(Key key);
    
    /**
     * Returns all {@link Key}s.
     */
    public Iterable<Key> keys();
    
    /**
     * Returns all {@link Key}s that are close to the lookup 
     * {@link KUID} but not any further than the last {@link KUID}.
     */
    public Iterable<Key> keys(KUID lookupId, KUID lastId);
    
    /**
     * Returns the size of the {@link Database}.
     */
    public int size();
    
    /**
     * Returns {@code true} if the {@link Database} is empty.
     */
    public boolean isEmpty();
    
    /**
     * 
     */
    public void forward(Contact dst, KUID lastId);
}