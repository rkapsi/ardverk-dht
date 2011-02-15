package org.ardverk.dht.storage;

import org.ardverk.dht.lang.Identifier;
import org.ardverk.dht.routing.Contact;
import org.ardverk.lang.Age;
import org.ardverk.lang.Epoch;

public interface Descriptor extends Identifier, Epoch, Age {

    /**
     * Returns the sender of the {@link ValueTuple}.
     */
    public Contact getSender();
    
    /**
     * Returns the creator of the {@link ValueTuple}.
     */
    public Contact getCreator();
}
