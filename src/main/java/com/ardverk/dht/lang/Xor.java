package com.ardverk.dht.lang;

/**
 * Objects that support the XOR operation may implement this interface
 */
public interface Xor<T> {

    /**
     * Returns the XOR of this and the other Object
     */
    public T xor(T other);
}
