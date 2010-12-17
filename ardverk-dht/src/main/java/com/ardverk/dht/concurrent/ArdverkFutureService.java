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

package com.ardverk.dht.concurrent;

import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.ExecutorKey;
import com.ardverk.dht.config.Config;

public interface ArdverkFutureService {

    /**
     * Submits the given {@link AsyncProcess} for execution.
     */
    public <V> ArdverkFuture<V> submit(
            AsyncProcess<V> process, Config config);
    
    /**
     * Submits the given {@link AsyncProcess} for execution.
     */
    public <V> ArdverkFuture<V> submit(ExecutorKey executorKey, 
            AsyncProcess<V> process, long timeout, TimeUnit unit);
}