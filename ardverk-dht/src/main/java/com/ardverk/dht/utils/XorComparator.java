package com.ardverk.dht.utils;

import java.util.Comparator;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.KUID;

/**
 * The {@link XorComparator} compares {@link KUID}s by their XOR distance.
 */
public class XorComparator implements Comparator<KUID> {

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
    public int compare(KUID o1, KUID o2) {
        return o1.xor(key).compareTo(o2.xor(key));
    }
}