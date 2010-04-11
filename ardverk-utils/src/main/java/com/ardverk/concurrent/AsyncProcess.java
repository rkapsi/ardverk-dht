/*
 * Copyright 2009 Roger Kapsi
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.ardverk.concurrent;

import java.util.concurrent.Callable;

import org.ardverk.concurrent.AsyncFuture;

/**
 * The {@link AsyncProcess} interface should be implemented by any
 * class whose instances are intended to be executed in the context
 * of an {@link AsyncFuture}.
 * 
 * @see Runnable
 * @see Callable
 */
public interface AsyncProcess<V> {

    /**
     * Starts the {@link AsyncProcess}
     * 
     * ATTENTION: This method is being called by the given {@link AsyncFuture}
     * while it is holding a lock on itself.
     */
    public void start(AsyncProcessFuture<V> future) throws Exception;
}
