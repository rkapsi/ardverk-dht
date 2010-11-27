package com.ardverk.dht.utils;

import java.util.Comparator;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.Identifier;
import com.ardverk.dht.KUID;

/**
 * The {@link XorComparator} compares {@link Identifier}s by their XOR distance.
 */
public class XorComparator implements Comparator<Identifier> {

    private final KUID key;
    
    public XorComparator(KUID key) {
        this.key = Arguments.notNull(key, "key");
    }
    
    /**
     * Returns the key.
     */
    public KUID getKey() {
        return key;
    }
    
    @Override
    public int compare(Identifier o1, Identifier o2) {
        return key.xor(o1.getId()).compareTo(key.xor(o2.getId()));
    }
}