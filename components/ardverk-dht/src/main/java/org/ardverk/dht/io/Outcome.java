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

package org.ardverk.dht.io;

import java.util.concurrent.TimeUnit;

import org.ardverk.dht.lang.Identifier;
import org.ardverk.dht.routing.Contact;


/**
 * The {@link Outcome} is a snapshot of the current lookup process.
 */
public abstract class Outcome implements Identifier {
    
    /**
     * Returns the k-closest {@link Contact}s that have been found.
     */
    public abstract Contact[] getClosest();
    
    /**
     * Returns all {@link Contact}s that have been found.
     */
    public abstract Contact[] getContacts();
    
    /**
     * Returns the number of hops the lookup has taken.
     */
    public abstract int getHop();
    
    /**
     * Returns the number of errors that have been occurred.
     */
    public abstract int getErrorCount();
    
    /**
     * Returns the lookup time in the given {@link TimeUnit}.
     */
    public abstract long getTime(TimeUnit unit);
    
    /**
     * Returns the lookup time in milliseconds.
     */
    public long getTimeInMillis() {
        return getTime(TimeUnit.MILLISECONDS);
    }
}