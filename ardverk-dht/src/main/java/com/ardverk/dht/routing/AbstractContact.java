package com.ardverk.dht.routing;

import com.ardverk.dht.KUID;

public abstract class AbstractContact implements Contact {

    private final long creationTime;
    
    private final long timeStamp = System.currentTimeMillis();
    
    private final KUID contactId;
    
    public AbstractContact(KUID contactId) {
        this(System.currentTimeMillis(), contactId);
    }
    
    public AbstractContact(long creationTime, KUID contactId) {
        if (contactId == null) {
            throw new NullPointerException("contactId");
        }
        
        this.creationTime = creationTime;
        this.contactId = contactId;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }
    
    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public KUID getContactId() {
        return contactId;
    }
}
