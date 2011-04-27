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

package org.ardverk.dht.http;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

class IdleUtils {

    private static final Timer TIMER = new HashedWheelTimer(new ThreadFactory() {
        
        private final AtomicInteger counter = new AtomicInteger();
        
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "IdleThread-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    });
    
    public static final IdleStateHandler DEFAULT 
        = createIdleStateHandler(30L, TimeUnit.SECONDS);
    
    public static IdleStateHandler createIdleStateHandler(
            long timeout, TimeUnit unit) {
        return createIdleStateHandler(0L, 0L, timeout, unit);
    }
    
    public static IdleStateHandler createIdleStateHandler(
            long r, long w, TimeUnit unit) {
        return createIdleStateHandler(r, w, 0L, unit);
    }
    
    public static IdleStateHandler createIdleStateHandler(
            long r, long w, long rw, TimeUnit unit) {
        return new IdleStateHandler(TIMER, r, w, rw, unit);
    }
}