package com.ardverk.dht.storage;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.lang.Identifier;
import com.ardverk.dht.routing.Contact;

public interface ValueTuple extends Identifier {

    /**
     * Returns the {@link ValueTuple}'s creation time.
     */
    public long getCreationTime();
    
    /**
     * Returns the {@link ValueTuple}'s age in the given {@link TimeUnit}.
     */
    public long getAge(TimeUnit unit);
    
    /**
     * Returns the {@link ValueTuple}'s age in milliseconds.
     */
    public long getAgeInMillis();
    
    /**
     * Returns the sender of the {@link ValueTuple}.
     */
    public Contact getSender();
    
    /**
     * Returns the creator of the {@link ValueTuple}.
     */
    public Contact getCreator();
    
    /**
     * Returns the {@link Value} of the {@link ValueTuple}.
     */
    public byte[] getValue();
    
    /**
     * Returns the size of the value.
     * 
     * @see #getValue()
     */
    public int size();
    
    /**
     * Returns {@code true} if the {@link Value} is empty.
     */
    public boolean isEmpty();
}