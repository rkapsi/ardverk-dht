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

package org.ardverk.dht.concurrent;

import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncProcess;
import org.ardverk.concurrent.AsyncProcessFutureTask;
import org.ardverk.concurrent.EventUtils;

/**
 * An implementation of {@link DHTFuture} that implements the 
 * {@link DHTRunnableFuture} interface and can hence be executed
 * on a {@link Thread}.
 */
public class DHTFutureTask<V> extends AsyncProcessFutureTask<V> 
        implements DHTRunnableFuture<V> {

    private volatile Object attachment;
    
    public DHTFutureTask() {
        super();
    }

    public DHTFutureTask(AsyncProcess<V> process, long timeout,
            TimeUnit unit) {
        super(process, timeout, unit);
    }

    public DHTFutureTask(AsyncProcess<V> process) {
        super(process);
    }
    
    @Override
    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    @Override
    public Object getAttachment() {
        return attachment;
    }

    @Override
    protected boolean isEventThread() {
        return EventUtils.isEventThread();
    }
    
    @Override
    protected void fireOperationComplete(final AsyncFutureListener<V> first,
            final AsyncFutureListener<V>... others) {
        
        if (first == null) {
            return;
        }
        
        Runnable event = new Runnable() {
            @Override
            public void run() {
                DHTFutureTask.super.fireOperationComplete(first, others);
            }
        };
        
        EventUtils.fireEvent(event);
    }
}