package com.ardverk.dht.routing;

import com.ardverk.dht.KUID;
import com.ardverk.dht.lang.Identifier;

/**
 * 
 */
public interface Bucket extends Identifier, Longevity {
    
    /**
     * Returns the {@link Bucket}'s depth in the {@link RouteTable} tree.
     */
    public int getDepth();
    
    /**
     * Returns the number of active {@link Contact}s in the {@link Bucket}.
     */
    public int getActiveCount();
    
    /**
     * Returns true if there are no active {@link Contact}s 
     * in the {@link Bucket}.
     */
    public boolean isActiveEmpty();
    
    /**
     * Returns the number of cached {@link Contact}s in the {@link Bucket}.
     */
    public int getCachedCount();
    
    /**
     * Returns true if there are no cached {@link Contact}s 
     * in the {@link Bucket}.
     */
    public boolean isCacheEmpty();
    
    /**
     * Returns all active {@link ContactEntity}ies.
     */
    public ContactEntity[] getActive();
    
    /**
     * Returns all cached {@link ContactEntity}ies.
     */
    public ContactEntity[] getCached();
    
    /**
     * Returns a {@link ContactEntity} for the given {@link KUID}.
     */
    public ContactEntity get(KUID contactId);
    
    /**
     * Returns an active {@link ContactEntity} for the given {@link KUID}.
     */
    public ContactEntity getActive(KUID contactId);
    
    /**
     * Returns a cached {@link ContactEntity} for the given {@link KUID}.
     */
    public ContactEntity getCached(KUID contactId);
    
    /**
     * Returns true if the {@link Bucket} contains a 
     * {@link Contact} with the given {@link KUID}.
     */
    public boolean contains(KUID contactId);
    
    /**
     * Returns true if the {@link Bucket} contains an active 
     * {@link Contact} with the given {@link KUID}.
     */
    public boolean containsActive(KUID contactId);
    
    /**
     * Returns true if the {@link Bucket} contains a cached 
     * {@link Contact} with the given {@link KUID}.
     */
    public boolean containsCached(KUID contactId);
}
