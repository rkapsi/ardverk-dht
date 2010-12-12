/*
 * Copyright 2010 Roger Kapsi
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

import com.ardverk.dht.KUID;
import com.ardverk.dht.lang.StringValue;


public interface Database {
    
    /**
     * Returned by {@link Database#store(ValueTuple)}.
     */
    public static interface Condition extends StringValue {
        
        /**
         * Returns {@code true} if a {@link ValueTuple} was stored 
         * successfully in the {@link Database}.
         */
        public boolean isSuccess();
    }
    
    public DatabaseConfig getDatabaseConfig();
    
    /**
     * Stores the given {@link ValueTuple} and returns a {@link Condition}.
     */
    public Condition store(ValueTuple tuple);
    
    /**
     * Returns a {@link ValueTuple} for the given {@link KUID}.
     */
    public ValueTuple get(KUID key);
    
    /**
     * Returns all {@link ValueTuple}s for the given {@link KUID}.
     */
    public ValueTuple[] select(KUID key);
    
    /**
     * Returns all {@link ValueTuple}s.
     */
    public ValueTuple[] values();
    
    /**
     * Returns the size of the {@link Database}.
     */
    public int size();
    
    /**
     * Returns {@code true} if the {@link Database} is empty.
     */
    public boolean isEmpty();
}