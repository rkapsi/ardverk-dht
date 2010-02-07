package com.ardverk.dht;

import org.ardverk.collection.KeyAnalyzer;

public class KUIDKeyAnalyzer implements KeyAnalyzer<KUID> {
    
    private static final long serialVersionUID = 4460777618233054271L;

    @Override
    public int bitIndex(KUID key, int offsetInBits, int lengthInBits,
            KUID otherId, int otherOffsetInBits, int otherLengthInBits) {
        int index = key.commonPrefix(otherId);
        if (index == lengthInBits) {
            if (key.isMin()) {
                return NULL_BIT_KEY;
            }
            
            return EQUAL_BIT_KEY;
        }
        
        return index;
    }

    @Override
    public int bitsPerElement() {
        return Byte.SIZE;
    }

    @Override
    public boolean isBitSet(KUID key, int bitIndex, int lengthInBits) {
        return key.isSet(bitIndex);
    }

    @Override
    public boolean isPrefix(KUID prefix, int offsetInBits, int lengthInBits,
            KUID key) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int lengthInBits(KUID key) {
        return key.lengthInBits();
    }

    @Override
    public int compare(KUID o1, KUID o2) {
        return o1.compareTo(o2);
    }
}
