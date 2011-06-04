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

import java.util.concurrent.atomic.AtomicReference;

import org.ardverk.dht.DHT;

/**
 * An abstract implementation of {@link Database}.
 */
public abstract class AbstractDatabase implements Database {
    
    private final AtomicReference<DHT> dhtRef = new AtomicReference<DHT>();
    
    protected DHT getDHT() {
        return dhtRef.get();
    }
    
    @Override
    public void bind(DHT dht) {
        dhtRef.set(dht);
    }

    @Override
    public boolean isBound() {
        return dhtRef.get() != null;
    }

    @Override
    public void unbind() {
        dhtRef.set(null);
    }
}