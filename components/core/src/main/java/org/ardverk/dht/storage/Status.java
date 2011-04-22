package org.ardverk.dht.storage;

import org.ardverk.dht.lang.IntegerValue;
import org.ardverk.dht.lang.StringValue;

/**
 * Returned by {@link Database#store(ValueTuple)}.
 */
public interface Status extends IntegerValue, StringValue {
    
    /**
     * The {@link Database} may return a {@link ValueTuple}.
     */
    public ValueTuple getValueTuple();
}