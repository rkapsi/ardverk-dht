package org.ardverk.dht.storage;

import java.util.concurrent.TimeUnit;

import org.ardverk.dht.KUID;
import org.ardverk.dht.routing.Contact;
import org.ardverk.lang.TimeStamp;
import org.ardverk.version.VectorClock;

public class DefaultValueResource implements ValueResource {

    private final TimeStamp creationTime = TimeStamp.now();
    
    private final ResourceId resourceId;
    
    private final Contact creator;
    
    private final Contact sender;
    
    private final VectorClock<KUID> clock;

    private final Value value;
    
    public DefaultValueResource(ResourceId resourceId, 
            Contact contact, VectorClock<KUID> clock, Value value) {
        this(resourceId, contact, contact, clock, value);
    }
    
    public DefaultValueResource(ResourceId resourceId, Contact sender,
            Contact creator, VectorClock<KUID> clock, Value value) {
        this.resourceId = resourceId;
        this.sender = sender;
        this.creator = pickCreator(sender, creator);
        this.clock = clock;
        this.value = value;
    }

    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public Contact getCreator() {
        return creator;
    }

    @Override
    public Contact getSender() {
        return sender;
    }

    @Override
    public VectorClock<KUID> getVectorClock() {
        return clock;
    }
    
    @Override
    public long getCreationTime() {
        return creationTime.getCreationTime();
    }

    @Override
    public long getAge(TimeUnit unit) {
        return creationTime.getAge(unit);
    }

    @Override
    public long getAgeInMillis() {
        return creationTime.getAgeInMillis();
    }

    @Override
    public Value getValue() {
        return value;
    }

    /**
     * To save memory we're trying to re-use the {@link Contact}
     * instance if sender and creator are the same.
     */
    private static Contact pickCreator(Contact sender, Contact creator) {
        if (creator == null || sender.equals(creator)) {
            return sender;
        }
        
        return creator;
    }
}
