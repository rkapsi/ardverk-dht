package com.ardverk.dht;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

import org.ardverk.collection.ByteArrayKeyAnalyzer;
import org.ardverk.collection.KeyAnalyzer;

import com.ardverk.io.Writable;

public class KUID implements Writable, Serializable, Comparable<KUID> {

    private static final long serialVersionUID = -4611363711131603626L;

    public static final int NO_BIT_MASK = -1;
    
    private final byte[] key;

    private final int bitMask;
    
    private final int lengthInBits;
    
    private final int hashCode;

    public KUID(byte[] key, int bitMask, int lengthInBits) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        
        if (key.length == 0) {
            throw new IllegalArgumentException("key.length=" + key.length);
        }
        
        if (lengthInBits <= 0 || (lengthInBits > (key.length * 8))) {
            throw new IllegalArgumentException("lengthInBits=" + lengthInBits);
        }
        
        this.key = key;
        this.bitMask = bitMask;
        this.lengthInBits = lengthInBits;
        
        if (bitMask != NO_BIT_MASK) {
            key[key.length-1] = (byte)(key[key.length-1] & bitMask);
        }
        
        this.hashCode = Arrays.hashCode(key);
    }

    public byte[] getBytes() {
        return key.clone();
    }
    
    public byte[] getBytes(byte[] dst, int destPos) {
        System.arraycopy(key, 0, dst, destPos, key.length);
        return dst;
    }
    
    public int length() {
        return key.length;
    }

    public int lengthInBits() {
        return lengthInBits;
    }
    
    public int bitMask() {
        return bitMask;
    }
    
    public KUID xor(KUID otherId) {
        if (otherId == null) {
            throw new NullPointerException("otherId");
        }
        
        int lengthInBits = lengthInBits();
        if (lengthInBits != otherId.lengthInBits()) {
            throw new IllegalArgumentException(
                    "lengthInBits=" + otherId.lengthInBits());
        }

        byte[] bytes = new byte[length()];
        for (int i = 0; i < key.length; i++) {
            bytes[i] = (byte) (key[i] ^ otherId.key[i]);
        }

        return new KUID(bytes, bitMask(), lengthInBits);
    }

    public KUID inverse() {
        byte[] bytes = new byte[length()];
        for (int i = 0; i < key.length; i++) {
            bytes[i] = (byte)(~key[i]);
        }
        
        return new KUID(bytes, bitMask(), lengthInBits());
    }
    
    public boolean isSet(int bitIndex) {
        if (bitIndex < 0 || bitIndex >= lengthInBits()) {
            throw new IllegalArgumentException("bitIndex=" + bitIndex);
        }
        
        int index = (int)(bitIndex / 8);
        if (index >= length()) {
            throw new IllegalArgumentException("bitIndex=" + bitIndex);
        }
        
        int bit = (int)(bitIndex % 8);
        return (key[index] & (0x80 >>> bit)) != 0x00;
    }
    
    public KUID set(int bitIndex) {
        return set(bitIndex, true);
    }
    
    public KUID unset(int bitIndex) {
        return set(bitIndex, false);
    }
    
    public KUID flip(int bitIndex) {
        return set(bitIndex, !isSet(bitIndex));
    }
    
    private KUID set(int bitIndex, boolean on) {
        int lengthInBits = lengthInBits();
        
        if (bitIndex < 0 || bitIndex >= lengthInBits) {
            throw new IllegalArgumentException("bitIndex=" + bitIndex);
        }
        
        int index = (int)(bitIndex / 8);
        if (index >= length()) {
            throw new IllegalArgumentException("bitIndex=" + bitIndex);
        }
        
        int bit = (int)(bitIndex % 8);
        int mask = (int)(0x80 >>> bit);
        int value = (int)(key[index] & 0xFF);
        
        if (on != ((value & mask) != 0x00)) {
            int bitMask = bitMask();
            byte[] copy = getBytes();
            
            if (on) {
                copy[index] = (byte)(value | mask);
            } else {
                copy[index] = (byte)(value & ~mask);
            }
            return new KUID(copy, bitMask, lengthInBits);
        }
        
        return this;
    }
    
    /**
     * Returns the common prefix length of the two {@link KUID}s.
     */   
    public int getPrefixLength(KUID otherId) {
        if (otherId == null) {
            throw new NullPointerException("otherId");
        }
        
        int lengthInBits = lengthInBits();
        if (lengthInBits != otherId.lengthInBits()) {
            throw new IllegalArgumentException(
                    "lengthInBits=" + lengthInBits 
                    + ", otherId.lengthInBits=" + otherId.lengthInBits());
        }
        
        for (int i = 0; i < lengthInBits; i++) {
            if (isSet(i) != otherId.isSet(i)) {
                return i;
            }
        }
        
        return lengthInBits;
    }
    
    @Override
    public int compareTo(KUID otherId) {
        return compareTo(otherId, lengthInBits());
    }

    public int compareTo(KUID otherId, int lengthInBits) {
        if (otherId == null) {
            throw new NullPointerException("otherId");
        }
        
        if (lengthInBits < 0 
                || lengthInBits > lengthInBits() 
                || lengthInBits > otherId.lengthInBits()) {
            throw new IllegalArgumentException("lengthInBits=" + lengthInBits);
        }
        
        if (this == otherId) {
            return 0;
        }
        
        int length = Math.min(length(), otherId.length());
        
        int index = (int)(lengthInBits / 8);
        int bits = (int)(lengthInBits % 8);
        int diff = 0;
        
        for (int i = 0; i < length; i++) {
            if (i == index && bits != 0) {
                for (int j = 0; j < bits; j++) {
                    diff = (key[i] & (0x80 >>> j)) - (otherId.key[i] & (0x80 >>> j));
                    if (diff != 0) {
                        return diff;
                    }
                }
            } else {
                diff = (key[i] & 0xFF) - (otherId.key[i] & 0xFF);
                if (diff != 0) {
                    return diff;
                }
            }
        }
        
        return 0;
    }
    
    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof KUID)) {
            return false;
        }

        KUID other = (KUID) obj;
        return Arrays.equals(key, other.key);
    }

    @Override
    public int write(OutputStream out) throws IOException {
        if (out == null) {
            throw new NullPointerException("out");
        }
        
        out.write(key);
        return length();
    }

    public BigInteger toBigInteger() {
        BigInteger value = new BigInteger(1 /* unsigned */, key);
        
        if (bitMask() != NO_BIT_MASK) {
            int bits = (lengthInBits % 8);
            value = value.shiftRight(bits);
        }
        
        return value;
    }
    
    public String toHexString() {
        return toBigInteger().toString(16);
    }
    
    @Override
    public String toString() {
        return toHexString();
    }
    
    public static KeyAnalyzer<KUID> createKeyAnalyzer(int maxLengthInBits) {
        return new KUIDKeyAnalyzer(maxLengthInBits);
    }
    
    private static class KUIDKeyAnalyzer implements KeyAnalyzer<KUID> {

        private static final long serialVersionUID = -7088064139086808955L;
        
        private final ByteArrayKeyAnalyzer keyAnalyzer;
        
        public KUIDKeyAnalyzer(int maxLengthInBits) {
            keyAnalyzer = new ByteArrayKeyAnalyzer(maxLengthInBits);
        }
        
        @Override
        public int bitIndex(KUID key, int offsetInBits, int lengthInBits,
                KUID other, int otherOffsetInBits, int otherLengthInBits) {
            
            return keyAnalyzer.bitIndex(key != null ? key.key : null, 
                    offsetInBits, 
                    lengthInBits, 
                    other != null ? other.key : null, 
                    otherOffsetInBits, 
                    otherLengthInBits);
        }

        @Override
        public int bitsPerElement() {
            return keyAnalyzer.bitsPerElement();
        }

        @Override
        public boolean isBitSet(KUID key, int bitIndex, int lengthInBits) {
            return keyAnalyzer.isBitSet(key != null ? key.key : null, 
                    bitIndex, lengthInBits);
        }

        @Override
        public boolean isPrefix(KUID prefix, int offsetInBits,
                int lengthInBits, KUID key) {
            return keyAnalyzer.isPrefix(prefix != null ? prefix.key : null, 
                    offsetInBits, lengthInBits, key != null ? key.key : null);
        }

        @Override
        public int lengthInBits(KUID key) {
            return keyAnalyzer.lengthInBits(key != null ? key.key : null);
        }

        @Override
        public int compare(KUID o1, KUID o2) {
            return keyAnalyzer.compare(o1 != null ? o1.key : null, 
                    o2 != null ? o2.key : null);
        }
    }
}
