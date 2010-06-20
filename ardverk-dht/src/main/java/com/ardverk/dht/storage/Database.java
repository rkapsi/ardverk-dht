package com.ardverk.dht.storage;


public interface Database {
    
    public static interface Condition {
        
        public boolean isSuccess();
        
        public String name();
    }
    
    /**
     * Stores the given {@link ValueTuple} and returns a {@link Condition}.
     */
    public Condition store(ValueTuple tuple);
    
    /**
     * Returns a {@link ValueTuple} for the given {@link Key}.
     */
    public ValueTuple get(Key key);
    
    /**
     * Returns all {@link ValueTuple}s for the given {@link Key}.
     */
    public ValueTuple[] select(Key key);
    
    /**
     * Returns all {@link ValueTuple}s.
     */
    public ValueTuple[] values();
    
    public int size();
    
    public boolean isEmpty();
}
