package com.ardverk.dht.storage;

import com.ardverk.dht.KUID;
import com.ardverk.dht.lang.StringValue;


public interface Database {
    
    /**
     * Returned by {@link Database#store(ValueTuple)}.
     */
    public static interface Condition extends StringValue {
        
        /**
         * Returns {@code true} if a {@link ValueTuple} was stored 
         * successfully in the {@link Database}.
         */
        public boolean isSuccess();
    }
    
    public DatabaseConfig getDatabaseConfig();
    
    /**
     * Stores the given {@link ValueTuple} and returns a {@link Condition}.
     */
    public Condition store(ValueTuple tuple);
    
    /**
     * Returns a {@link ValueTuple} for the given {@link KUID}.
     */
    public ValueTuple get(KUID key);
    
    /**
     * Returns all {@link ValueTuple}s for the given {@link KUID}.
     */
    public ValueTuple[] select(KUID key);
    
    /**
     * Returns all {@link ValueTuple}s.
     */
    public ValueTuple[] values();
    
    /**
     * Returns the size of the {@link Database}.
     */
    public int size();
    
    /**
     * Returns {@code true} if the {@link Database} is empty.
     */
    public boolean isEmpty();
}
