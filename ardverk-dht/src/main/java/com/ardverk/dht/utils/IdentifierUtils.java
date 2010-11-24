package com.ardverk.dht.utils;

import java.util.Arrays;
import java.util.Comparator;

import org.ardverk.utils.ReverseComparator;

import com.ardverk.dht.Identifier;
import com.ardverk.dht.KUID;

public class IdentifierUtils {

    private static final Comparator<ValueRef<?>> XOR_ASCENDING 
        = new Comparator<ValueRef<?>>() {
            @Override
            public int compare(ValueRef<?> o1, ValueRef<?> o2) {
                return o1.xor.compareTo(o2.xor);
            }
        };
    
    private static final Comparator<ValueRef<?>> XOR_DESCENDING 
        = new ReverseComparator<ValueRef<?>>(XOR_ASCENDING);

    private IdentifierUtils() {}
    
    /**
     * Sorts the given {@link Identifier}'s by XOR distance.
     */
    public static <T extends Identifier> T[] byXor(T[] values, Identifier key) {
        return byXor(values, key, true);
    }
    
    /**
     * Sorts the given {@link Identifier}'s by XOR distance.
     */
    public static <T extends Identifier> T[] byXor(T[] values, 
            Identifier key, boolean ascending) {
        
        // NOTE: It's relatively expensive to compute the XOR distances!
        //   We're therefore computing them only *ONCE* and use an
        //   intermediate array as a cache and we're sorting the cache 
        //   array instead. 
        @SuppressWarnings("unchecked")
        ValueRef<T>[] valueRefs = new ValueRef[values.length];
        
        KUID id = key.getId();
        for (int i = values.length-1; i >= 0; --i) {
            valueRefs[i] = new ValueRef<T>(values[i], id);
        }
        
        Arrays.sort(valueRefs, ascending ? XOR_ASCENDING : XOR_DESCENDING);
        
        for (int i = valueRefs.length-1; i >= 0; --i) {
            values[i] = valueRefs[i].value;
        }
        
        return values;
    }
    
    private static class ValueRef<T extends Identifier> {
        
        private final T value;
        
        private final KUID xor;
        
        public ValueRef(T value, KUID key) {
            this.value = value;
            this.xor = key.xor(value.getId());
        }
    }
}
