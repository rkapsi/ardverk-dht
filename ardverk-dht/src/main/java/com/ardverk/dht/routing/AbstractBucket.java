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

import com.ardverk.dht.KUID;

/**
 * An abstract implementation of {@link Bucket}.
 */
public abstract class AbstractBucket implements Bucket {

    private final long creationTime = System.currentTimeMillis();
    
    private final KUID bucketId;
    
    private final int depth;
    
    private long timeStamp = creationTime;
    
    public AbstractBucket(KUID bucketId, int depth) {
        this.bucketId = bucketId;
        this.depth = depth;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }
    
    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets the {@link Bucket}'s time stamp
     */
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
    
    /**
     * Sets the {@link Bucket}'s time stamp to <tt>now</tt>
     * 
     * @see System#currentTimeMillis()
     */
    public void touch() {
        setTimeStamp(System.currentTimeMillis());
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
    public ContactEntity get(KUID contactId) {
        ContactEntity entity = getActive(contactId);
        if (entity == null) {
            entity = getCached(contactId);
        }
        return entity;
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