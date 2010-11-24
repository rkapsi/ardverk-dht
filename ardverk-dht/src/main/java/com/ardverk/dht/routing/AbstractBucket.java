package com.ardverk.dht.routing;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;

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
    
    protected void touch() {
        timeStamp = System.currentTimeMillis();
    }
    
    protected boolean isTimeout(long timeout, TimeUnit unit) {
        long timeoutInMillis = unit.toMillis(timeout);
        return (System.currentTimeMillis()-timeStamp) >= timeoutInMillis;
    }

    @Override
    public KUID getBucketId() {
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
