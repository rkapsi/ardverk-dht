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

package org.ardverk.dht.io;

import java.util.concurrent.TimeUnit;

import org.ardverk.dht.lang.DHTException;
import org.ardverk.dht.storage.ValueTuple;


/**
 * The {@link StoreException} is thrown if a {@link ValueTuple} couldn't
 * be stored in the DHT at all.
 */
public class StoreException extends DHTException {
    
    private static final long serialVersionUID = -1874658787780091708L;

    private final ValueTuple tuple;
    
    public StoreException(ValueTuple tuple, long time, TimeUnit unit) {
        super(time, unit);
        this.tuple = tuple;
    }

    /**
     * Returns the {@link ValueTuple} that failed to be stored.
     */
    public ValueTuple getValueTuple() {
        return tuple;
    }
}