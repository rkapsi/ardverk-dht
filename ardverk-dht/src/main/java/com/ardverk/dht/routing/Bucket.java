package com.ardverk.dht.routing;

import com.ardverk.dht.KUID;

public interface Bucket {

    public long getCreationTime();
    
    public long getTimeStamp();
    
    public KUID getBucketId();
    
    public int getDepth();
    
    public int getActiveCount();
    
    public boolean isActiveEmpty();
    
    public int getCachedCount();
    
    public boolean isCacheEmpty();
    
    public ContactEntity[] getActive();
    
    public ContactEntity[] getCached();
    
    public ContactEntity get(KUID contactId);
    
    public ContactEntity getActive(KUID contactId);
    
    public ContactEntity getCached(KUID contactId);
    
    public boolean contains(KUID contactId);
    
    public boolean containsActive(KUID contactId);
    
    public boolean containsCached(KUID contactId);
}
