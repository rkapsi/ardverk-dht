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

package org.ardverk.dht.routing;

import org.ardverk.dht.KUID;
import org.ardverk.lang.TimeStamp;


/**
 * An abstract implementation of {@link Bucket}.
 */
public abstract class AbstractBucket implements Bucket {

    private final TimeStamp creationTime = TimeStamp.now();
    
    private final KUID bucketId;
    
    private final int depth;
    
    private TimeStamp timeStamp = creationTime;
    
    public AbstractBucket(KUID bucketId, int depth) {
        this.bucketId = bucketId;
        this.depth = depth;
    }

    @Override
    public TimeStamp getCreationTime() {
        return creationTime;
    }
    
    @Override
    public TimeStamp getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets the {@link Bucket}'s time stamp
     */
    public void setTimeStamp(TimeStamp timeStamp) {
        this.timeStamp = timeStamp;
    }
    
    /**
     * Sets the {@link Bucket}'s time stamp to <tt>now</tt>
     * 
     * @see System#currentTimeMillis()
     */
    public void touch() {
        setTimeStamp(TimeStamp.now());
    }
    
    @Override
    public KUID getId() {
        return bucketId;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public ContactEntry get(KUID contactId) {
        ContactEntry entry = getActive(contactId);
        if (entry == null) {
            entry = getCached(contactId);
        }
        return entry;
    }

    @Override
    public boolean contains(KUID contactId) {
        return containsActive(contactId) || containsCached(contactId);
    }

    @Override
    public boolean isActiveEmpty() {
        return getActiveCount() == 0;
    }

    @Override
    public boolean isCacheEmpty() {
        return getCachedCount() == 0;
    }
}