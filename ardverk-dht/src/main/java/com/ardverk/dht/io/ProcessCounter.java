/*
 * Copyright 2010 Roger Kapsi
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

package com.ardverk.dht.io;

/**
 * A counter that keeps track of in parallel running processes
 */
class ProcessCounter {
    
    private final int maxProcesses;
    
    private int processes = 0;
    
    private int count = 0;
    
    public ProcessCounter(int maxProcesses) {
        if (maxProcesses < 0) {
            throw new IllegalArgumentException(
                    "maxProcesses=" + maxProcesses);
        }
        
        this.maxProcesses = maxProcesses;
    }
    
    public boolean hasNext() {
        return processes < maxProcesses;
    }
    
    public boolean increment() {
        return increment(false);
    }
    
    public boolean increment(boolean force) {
        if (processes < maxProcesses || force) {
            ++processes;
            ++count;
            return true;
        }
        return false;
    }
    
    public void decrement() {
        if (0 < processes) {
            --processes;
        }
    }
    
    public int getProcesses() {
        return processes;
    }
    
    public int getCount() {
        return count;
    }
}