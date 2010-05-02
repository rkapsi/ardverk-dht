package com.ardverk.dht.routing;

import java.net.SocketAddress;

import com.ardverk.dht.DHT;
import com.ardverk.dht.KUID;

public interface RouteTable {
    
    public void bind(DHT dht);
    
    public void unbind();
    
    public boolean isBound();
    
    public Contact2 getLocalhost();
    
    public void add(Contact2 contact);
    
    public int getK();
    
    /**
     * 
     */
    public Contact2 get(KUID contactId);
    
    /**
     * See {@link #select(KUID, int)}
     */
    public Contact2[] select(KUID contactId);
    
    /**
     * Returns up to <i>count</i> number of {@link Contact}s that are
     * XOR bit-wise closest to the given {@link KUID}. The {@link Contact}s
     * array is ordered by closeness.
     */
    public Contact2[] select(KUID contactId, int count);
    
    public void failure(KUID contactId, SocketAddress address);
    
    public void rebuild();
    
    public void addRouteTableListener(RouteTableListener l);
    
    public void removeRouteTableListener(RouteTableListener l);
    
    public RouteTableListener[] getRouteTableListeners();
    
    public int size();
}
