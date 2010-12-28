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

package com.ardverk.dht.routing;

import org.ardverk.lang.TimeStamp;

/**
 * Objects that have a persistent creation time and update
 * over time may implement this interface.
 */
public interface Longevity {

    /**
     * Returns the object's creation time.
     */
    public TimeStamp getCreationTime();
    
    /**
     * Returns the time when this object was modified.
     */
    public TimeStamp getTimeStamp();
}