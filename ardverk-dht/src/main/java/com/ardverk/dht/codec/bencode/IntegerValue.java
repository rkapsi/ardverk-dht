package com.ardverk.dht.codec.bencode;

/**
 * An interface meant to be implemented only by {@link Enum}s.
 */
interface IntegerValue {

    /**
     * Returns the int value of an {@link Enum}.
     */
    public int intValue();
}
