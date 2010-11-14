package com.ardverk.dht.utils;

import java.util.Comparator;

import org.ardverk.lang.Arguments;
import org.ardverk.lang.NullArgumentException;

import com.ardverk.dht.KUID;

public class XorComparator implements Comparator<KUID> {

    private final KUID key;
    
    public XorComparator(KUID key) {
        this.key = Arguments.notNull(key, "key");
    }
    
    @Override
    public int compare(KUID o1, KUID o2) {
        return o1.xor(key).compareTo(o2.xor(key));
    }
}