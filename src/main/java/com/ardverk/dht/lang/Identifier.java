package com.ardverk.dht.lang;

import com.ardverk.dht.KUID;

/**
 * An interface that provides an unique {@link KUID} identifier for an Object.
 */
public interface Identifier {

    /**
     * Returns the {@link KUID}
     */
    public KUID getId();
}
