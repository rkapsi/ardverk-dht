package com.ardverk.dht.routing;

import com.ardverk.dht.ContactPinger;
import com.ardverk.dht.KUID;
import com.ardverk.dht.KeyFactory;
import com.ardverk.dht.routing.DefaultRouteTable.Bucket;

public abstract class AbstractRouteTable implements RouteTable {
    
    protected final ContactFactory contactFactory;
    
    protected final int k;
    
    protected transient ContactPinger pinger;
    
    public AbstractRouteTable(ContactFactory contactFactory, int k) {
        if (contactFactory == null) {
            throw new NullPointerException("contactFactory");
        }
        
        if (k <= 0) {
            throw new IllegalArgumentException("k=" + k);
        }
        
        this.contactFactory = contactFactory;
        this.k = k;
    }
    
    @Override
    public ContactFactory getContactFactory() {
        return contactFactory;
    }
    
    @Override
    public KeyFactory getKeyFactory() {
        return getContactFactory().getKeyFactory();
    }
    
    @Override
    public int getK() {
        return k;
    }
    
    @Override
    public Contact[] select(KUID contactId) {
        return select(contactId, getK());
    }
    
    @Override
    public synchronized void setContactPinger(ContactPinger pinger) {
        this.pinger = pinger;
    }

    protected void fireSplitBucket(Bucket bucket, Bucket left, Bucket right) {
        
    }
    
    protected void fireContactAdded(Bucket bucket, Contact contact) {
        
    }
    
    protected void fireContactReplaced(Bucket bucket, Contact existing, Contact contact) {
        
    }
    
    protected void fireContactChanged(Bucket bucket, Contact existing, Contact contact) {
        
    }
}
