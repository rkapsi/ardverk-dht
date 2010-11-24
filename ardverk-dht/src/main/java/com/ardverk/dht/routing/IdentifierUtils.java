package com.ardverk.dht.routing;

import java.util.Arrays;
import java.util.Comparator;

import org.ardverk.utils.ReverseComparator;

import com.ardverk.dht.KUID;

public class IdentifierUtils {

    private static final Comparator<IdentifierRef<?>> XOR_ASCENDING 
        = new Comparator<IdentifierRef<?>>() {
            @Override
            public int compare(IdentifierRef<?> o1, IdentifierRef<?> o2) {
                return o1.xor.compareTo(o2.xor);
            }
        };
    
    private static final Comparator<IdentifierRef<?>> XOR_DESCENDING 
        = new ReverseComparator<IdentifierRef<?>>(XOR_ASCENDING);

    private IdentifierUtils() {}
    
    /**
     * Sorts the given {@link Identifier}'s by XOR distance.
     */
    public static <T extends Identifier> T[] byXor(T[] identifiers, KUID key) {
        return byXor(identifiers, key, true);
    }
    
    /**
     * Sorts the given {@link Identifier}'s by XOR distance.
     */
    public static <T extends Identifier> T[] byXor(T[] identifiers, 
            KUID key, boolean ascending) {
        
        // Assumption: It's cheaper to create the IdentifierRefs and
        // compute the XORs once instead of having to compute the XORs
        // over and over throughout the sorting process.
        @SuppressWarnings("unchecked")
        IdentifierRef<T>[] handles = new IdentifierRef[identifiers.length];
        for (int i = 0; i < identifiers.length; i++) {
            handles[i] = new IdentifierRef<T>(identifiers[i], key);
        }
        
        Arrays.sort(handles, ascending ? XOR_ASCENDING : XOR_DESCENDING);
        
        for (int i = 0; i < handles.length; i++) {
            identifiers[i] = handles[i].identifier;
        }
        
        return identifiers;
    }
    
    private static class IdentifierRef<T extends Identifier> {
        
        private final T identifier;
        
        private final KUID xor;
        
        public IdentifierRef(T identifier, KUID key) {
            this.identifier = identifier;
            this.xor = key.xor(identifier.getId());
        }
    }
}
