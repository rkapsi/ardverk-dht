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

import java.util.concurrent.TimeUnit;

/**
 * A mix-in interface for {@link AsyncProcess}. Instances of 
 * {@link AsyncProcess} can implement this interface to gain
 * control over the scheduling of a {@link AsyncFuture}'s watchdog
 * and use it to delay timeouts.
 * 
 * This is useful for {@link AsyncProcess}es that have no predictable
 * timeout but can ensure they're active.
 */
public interface Delay {

    /**
     * Return values greater than zero will reschedule the 
     * watchdog {@link Thread}.
     */
    public long getDelay(TimeUnit unit);
}
