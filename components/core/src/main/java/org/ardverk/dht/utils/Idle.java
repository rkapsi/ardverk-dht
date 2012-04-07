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

package org.ardverk.dht.utils;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.ardverk.io.ProgressInputStream.ProgressCallback;

public class Idle implements ProgressCallback {
    
    private boolean done = false;
    
    private long timeStamp = System.currentTimeMillis();
    
    @Override
    public synchronized void in(InputStream in, int count) {
        timeStamp = System.currentTimeMillis();
    }

    @Override
    public synchronized void eof(InputStream in) {
        done = true;
        notifyAll();
    }

    @Override
    public synchronized void closed(InputStream in) {
        done = true;
        notifyAll();
    }

    public synchronized void await(long timeout, TimeUnit unit) 
            throws InterruptedException {
        long timeoutInMillis = unit.toMillis(timeout);
        
        while (!done) {
            long time = System.currentTimeMillis() - timeStamp;
            if (time >= timeoutInMillis) {
                done = true;
                break;
            }
            
            wait(timeoutInMillis);
        }
    }
}
