package com.ardverk.collection;

/**
 * An interface for ADTs that have a fixed size capacity.
 */
public interface FixedSize {

    /**
     * Returns the maximum size of the ADT.
     */
    public int getMaxSize();
    
    /**
     * Returns true if the ADT is full.
     */
    public boolean isFull();
}
