package com.ardverk.dht.lang;

/**
 * Objects that support the bitwise negation operation may 
 * implement this interface
 */

public interface Negation<T> {

    /**
     * Returns the bitwise negation of this value
     */
    public T negate();
}
