/*
 * Copyright 2009-2012 Roger Kapsi
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

package org.ardverk.dht.concurrent;

import org.ardverk.concurrent.AsyncFutureListener;

/**
 * A {@link NopFuture} is a {@link DHTFuture} that has a pre-defined
 * value and doesn't keep track of its {@link AsyncFutureListener}s.
 */
public class NopFuture<T> extends DHTValueFuture<T> {
    
    public static <T> NopFuture<T> withValue(T value) {
        return new NopFuture<T>(value);
    }
    
    public static <T> NopFuture<T> withExcepton(Throwable t) {
        return new NopFuture<T>(t);
    }
    
    private NopFuture(T value) {
        super(value);
    }

    private NopFuture(Throwable t) {
        super(t);
    }

    /**
     * Overwritten to notify the {@link AsyncFutureListener} right away. It
     * won't be added to the internal list of {@link AsyncFutureListener}s!
     */
    @Override
    public void addAsyncFutureListener(AsyncFutureListener<T> l) {
        fireOperationComplete(l);
    }
}