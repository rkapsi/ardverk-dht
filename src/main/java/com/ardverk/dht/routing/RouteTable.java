package com.ardverk.dht.routing;

import java.net.SocketAddress;

import com.ardverk.dht.KUID;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.PingConfig;
import com.ardverk.dht.entity.PingEntity;

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
     * Returns all {@link Bucket}s.
     */
    public Bucket[] getBuckets();
    
    public void failure(KUID contactId, SocketAddress address);
    
    /**
     * Adds the given {@link RouteTableListener}.
     */
    public void addRouteTableListener(RouteTableListener l);
    
    /**
     * Removes the given {@link RouteTableListener}.
     */
    public void removeRouteTableListener(RouteTableListener l);
    
    /**
     * Returns all {@link RouteTableListener}s.
     */
    public RouteTableListener[] getRouteTableListeners();
    
    public int size();
    
    /**
     * A callback interface the {@link RouteTable} uses to send 
     * ping requests to {@link Contact}s.
     */
    public static interface ContactPinger {

        /**
         * Sends a ping to the given {@link Contact}.
         */
        public ArdverkFuture<PingEntity> ping(Contact contact, PingConfig config);
    }
}
