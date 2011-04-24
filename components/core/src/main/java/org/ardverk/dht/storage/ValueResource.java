package org.ardverk.dht.storage;

import org.ardverk.dht.KUID;
import org.ardverk.dht.routing.Contact;
import org.ardverk.lang.Age;
import org.ardverk.lang.Epoch;
import org.ardverk.version.VectorClock;

public interface ValueResource extends Resource, Epoch, Age {

    public Contact getCreator();
    
    public VectorClock<KUID> getVectorClock();
    
    public Value getValue();
}
