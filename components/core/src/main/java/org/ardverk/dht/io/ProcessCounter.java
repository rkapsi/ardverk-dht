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

/**
 * A counter that keeps track of in parallel running processes and is
 * limiting the number of concurrently active processes.
 */
class ProcessCounter {
    
    private final int maxActive;
    
    private int active = 0;
    
    private int count = 0;
    
    public ProcessCounter(int maxActive) {
        if (maxActive < 0) {
            throw new IllegalArgumentException(
                    "maxActive=" + maxActive);
        }
        
        this.maxActive = maxActive;
    }
    
    public boolean hasNext() {
        return active < maxActive;
    }
    
    public boolean increment() {
        return increment(false);
    }
    
    public boolean increment(boolean force) {
        if (active < maxActive || force) {
            ++active;
            ++count;
            return true;
        }
        return false;
    }
    
    public void decrement() {
        if (0 < active) {
            --active;
        }
    }
    
    public boolean hasActive() {
        return 0 < active;
    }
    
    public int getActive() {
        return active;
    }
    
    public int getCount() {
        return count;
    }
}