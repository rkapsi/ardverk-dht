package com.ardverk.dht.routing;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.Identifier;
import com.ardverk.dht.KUID;

public interface Bucket extends Identifier {

    public long getCreationTime();
    
    public long getTimeStamp();
    
    public boolean isTimeout(long timeout, TimeUnit unit);
    
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
