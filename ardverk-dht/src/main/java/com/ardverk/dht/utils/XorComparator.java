package com.ardverk.dht.utils;

import java.util.Comparator;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.KUID;
import com.ardverk.dht.lang.Identifier;

/**
 * The {@link XorComparator} compares {@link Identifier}s by their XOR distance.
 */
public class XorComparator implements Comparator<Identifier> {

    private final Identifier identifier;
    
    public XorComparator(Identifier identifier) {
        this.identifier = Arguments.notNull(identifier, "identifier");
    }
    
    /**
     * Returns the {@link Identifier}.
     */
    public Identifier getIdentifier() {
        return identifier;
    }
    
    private KUID xor(Identifier id) {
        return identifier.getId().xor(id.getId());
    }
    
    @Override
    public int compare(Identifier o1, Identifier o2) {
        return xor(o1).compareTo(xor(o2));
    }
}