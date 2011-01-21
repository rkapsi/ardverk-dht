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

package org.ardverk.dht.event;

import java.util.ServiceLoader;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

import org.ardverk.concurrent.DefaultThreadFactory;
import org.ardverk.lang.NullArgumentException;

/**
 * The {@link EventUtils} class provides a simple interface to fire
 * events. It provides a SPI interface that allows users to replace
 * the default event {@link Thread} if necessary.
 */
public class EventUtils {

    private static final EventThreadProvider PROVIDER;
    
    static {
        EventThreadProvider provider = null;
        for (EventThreadProvider element 
                : ServiceLoader.load(
                        EventThreadProvider.class)) {
            provider = element;
            break;
        }
        
        if (provider == null) {
            provider = new DefaultEventThreadProvider();
        }
      
    
        PROVIDER = provider;
    }
    
    private EventUtils() {}

    /**
     * Returns true if the caller {@link Thread} is the same as the event 
     * {@link Thread}. In other words {@link Thread}s can use method to 
     * determinate if they are the event {@link Thread}.
     */
    public static boolean isEventThread() {
        return PROVIDER.isEventThread();
    }

    /**
     * Executes the given {@link Runnable} on the event {@link Thread}.
     */
    public static void fireEvent(Runnable event) {
        PROVIDER.fireEvent(event);
    }
    
    /**
     * The default event {@link Thread} provider that is being used 
     * if no other {@link EventThreadProvider} was given.
     */
    private static class DefaultEventThreadProvider 
            implements EventThreadProvider {

        private final AtomicReference<Thread> reference 
            = new AtomicReference<Thread>();
        
        private final ThreadFactory factory 
                = new DefaultThreadFactory("DefaultEventThread") {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = super.newThread(r);
                reference.set(thread);
                return thread;
            } 
        };
        
        private final Executor executor 
            = Executors.newSingleThreadExecutor(factory);
        
        @Override
        public boolean isEventThread() {
            return reference.get() == Thread.currentThread();
        }
        
        @Override
        public void fireEvent(Runnable event) {
            if (event == null) {
                throw new NullArgumentException("event");
            }
            
            executor.execute(event);
        }
    }
}