package com.ardverk.dht.codec.bencode;

/**
 * An interface meant to be implemented only by {@link Enum}s.
 */
interface StringValue {

    /**
     * Returns the {@link String} value of an {@link Enum}.
     */
    public String stringValue();
}
