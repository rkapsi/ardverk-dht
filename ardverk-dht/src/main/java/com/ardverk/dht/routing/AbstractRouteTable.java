package com.ardverk.dht.routing;

import com.ardverk.dht.KUID;
import com.ardverk.dht.KeyFactory;
import com.ardverk.dht.routing.DefaultRouteTable.Bucket;


public abstract class AbstractRouteTable implements RouteTable {
    
    private static final long serialVersionUID = 1610290233049496587L;

    private final ContactFactory contactFactory;
    
    private final int k;
    
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
    
    protected void fireSplitBucket(Bucket bucket, Bucket left, Bucket right) {
        
    }
}
