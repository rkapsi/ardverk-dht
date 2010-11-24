package com.ardverk.dht.routing;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.ContactPinger;
import com.ardverk.dht.KUID;

public interface RouteTable {
    
    public int getK();
    
    public void bind(ContactPinger pinger);
    
    public void unbind();
    
    public boolean isBound();
    
    public Contact getLocalhost();
    
    public void add(Contact contact);
    
    /**
     * 
     */
    public Contact get(KUID contactId);
    
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
    
    /**
     * Returns a set of prefixed random {@link KUID}s that need to be
     * looked up to keep the {@link RouteTable} fresh.
     */
    public KUID[] refresh(long timeout, TimeUnit unit);
    
    public Bucket[] getBuckets();
    
    public void failure(KUID contactId, SocketAddress address);
    
    public void addRouteTableListener(RouteTableListener l);
    
    public void removeRouteTableListener(RouteTableListener l);
    
    public RouteTableListener[] getRouteTableListeners();
    
    public int size();
}
