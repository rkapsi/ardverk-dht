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

package org.ardverk.dht.concurrent;

import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncValueFuture;
import org.ardverk.dht.event.EventUtils;

/**
 * 
 */
public class DHTValueFuture<V> extends AsyncValueFuture<V> 
        implements DHTFuture<V> {

    private volatile Object attachment;
    
    public DHTValueFuture() {
        super();
    }

    public DHTValueFuture(Throwable t) {
        super(t);
    }

    public DHTValueFuture(V value) {
        super(value);
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
    public long getTimeout(TimeUnit unit) {
        return 0;
    }

    @Override
    public long getTimeoutInMillis() {
        return getTimeout(TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isTimeout() {
        return false;
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
                DHTValueFuture.super.fireOperationComplete(first, others);
            }
        };
        
        EventUtils.fireEvent(event);
    }
}