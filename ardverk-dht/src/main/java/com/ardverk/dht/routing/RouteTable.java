package com.ardverk.dht.routing;

import java.net.SocketAddress;

import com.ardverk.dht.ContactPinger;
import com.ardverk.dht.KUID;
import com.ardverk.dht.KeyFactory;

public interface RouteTable {
    
    public Contact getLocalhost();
    
    public void setContactPinger(ContactPinger pinger);
    
    public void add(Contact contact);
    
    public ContactFactory getContactFactory();
    
    public KeyFactory getKeyFactory() ;
    
    public int getK();
    
    /**
     * See {@link #select(KUID, int)}
     */
    public Contact[] select(KUID contactId);
    
    /**
     * Returns up to <i>count</i> number of {@link Contact}s that are
     * XOR bit-wise closest to the given {@link KUID}. The {@link Contact}s
     * array is ordered by closeness.
     */
    public Contact[] select(KUID contactId, int count);
    
    public void failure(KUID contactId, SocketAddress address);
    
    public void rebuild();
}
