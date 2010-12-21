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

package com.ardverk.dht;

/**
 * The {@link ExecutorKey} controls how a particular operation should
 * be executed.
 */
public enum ExecutorKey {
    
    /**
     * The {@link #SERIAL} {@link ExecutorKey} executions enqueued operations
     * in a serial fashion.
     */
    SERIAL,
    
    /**
     * The {@link #PARALLEL} {@link ExecutorKey} executions enqueued operations
     * in a parallel fashion.
     */
    PARALLEL;
    
    /**
     * The default {@link ExecutorKey} that should be used unless there
     * is a reason not to use this {@link ExecutorKey}.
     */
    public static final ExecutorKey DEFAULT = ExecutorKey.PARALLEL;
    
    /**
     * The {@link ExecutorKey} that should be used for backend and possibly 
     * for other low priority operations.
     */
    public static final ExecutorKey BACKEND = ExecutorKey.SERIAL;
}