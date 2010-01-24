package com.ardverk.dht;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

import org.apache.commons.lang.NullArgumentException;
import org.ardverk.collection.ByteArrayKeyAnalyzer;
import org.ardverk.collection.KeyAnalyzer;

import com.ardverk.coding.CodingUtils;
import com.ardverk.io.Writable;
import com.ardverk.lang.Negation;
import com.ardverk.lang.Xor;

/**
 * Kademlia Unique Identifier ({@link KUID}) 
 */
public class KUID implements Xor<KUID>, Negation<KUID>, 
        Writable, Serializable, Comparable<KUID> {

    private static final long serialVersionUID = -4611363711131603626L;
    
    private static final int MSB_MASK = 0x80;
    
    private final byte[] key;
    
    private final int hashCode;

    public KUID(byte[] key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        
        if (key.length == 0) {
            throw new IllegalArgumentException(
                    "key.length=" + key.length);
        }
        
        this.key = key;
        this.hashCode = Arrays.hashCode(key);
    }

    /**
     * Returns the {@link KUID}'s bytes.
     */
    public byte[] getBytes() {
        return key.clone();
    }
    
    /**
     * Copies the {@link KUID}'s bytes into the given byte array.
     */
    public byte[] getBytes(byte[] dst, int destPos) {
        System.arraycopy(key, 0, dst, destPos, key.length);
        return dst;
    }
    
    /**
     * Returns the length of the {@link KUID} in bytes.
     */
    public int length() {
        return key.length;
    }

    /**
     * Returns the length of the {@link KUID} in bits.
     */
    public int lengthInBits() {
        return length() * 8;
    }
    
    @Override
    public KUID xor(KUID otherId) {
        if (otherId == null) {
            throw new NullPointerException("otherId");
        }
        
        int lengthInBits = lengthInBits();
        if (lengthInBits != otherId.lengthInBits()) {
            throw new IllegalArgumentException(
                    "lengthInBits=" + otherId.lengthInBits());
        }

        byte[] data = new byte[length()];
        for (int i = 0; i < key.length; i++) {
            data[i] = (byte) (key[i] ^ otherId.key[i]);
        }

        return new KUID(data);
    }

    @Override
    public KUID negate() {
        byte[] data = new byte[length()];
        for (int i = 0; i < key.length; i++) {
            data[i] = (byte)(~key[i]);
        }
        
        return new KUID(data);
    }
    
    /**
     * Returns the minimum {@link KUID}.
     */
    public KUID min() {
        byte[] minKey = new byte[length()];
        return new KUID(minKey);
    }
    
    /**
     * Returns the maximum {@link KUID}.
     */
    public KUID max() {
        byte[] maxKey = new byte[length()];
        Arrays.fill(maxKey, (byte)0xFF);
        return new KUID(maxKey);
    }
    
    /**
     * Returns true if the bit at the given bitIndex position is true (one, 1).
     */
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
    
    /**
     * Sets the bit at the given bitIndex position to true (one, 1) 
     * and returns the {@link KUID}.
     */
    public KUID set(int bitIndex) {
        return set(bitIndex, true);
    }
    
    /**
     * Sets the bit at the given bitIndex position to false (zero, 0) 
     * and returns the {@link KUID}.
     */
    public KUID unset(int bitIndex) {
        return set(bitIndex, false);
    }
    
    /**
     * Flips the bit at the given bitIndex position and returns the 
     * {@link KUID}.
     */
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
            byte[] copy = getBytes();
            
            if (on) {
                copy[index] = (byte)(value | mask);
            } else {
                copy[index] = (byte)(value & ~mask);
            }
            return new KUID(copy);
        }
        
        return this;
    }
    
    public int diff(KUID otherId) {
        if (otherId == null) {
            throw new NullPointerException("otherId");
        }
        
        int lengthInBits = lengthInBits();
        if (lengthInBits != otherId.lengthInBits()) {
            throw new IllegalArgumentException("otherId=" + otherId);
        }
        
        int bitIndex = 0;
        for (int i = 0; i < key.length; i++) {
            int value = (int)(key[i] ^ otherId.key[i]);
            
            for (int j = 0; j < 8 && bitIndex < lengthInBits; j++) {
                if ((value & (MSB_MASK >>> j)) != 0) {
                    return bitIndex;
                }
                
                ++bitIndex;
            }
        }
        
        return -1;
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
    
    /**
     * Returns true if this {@link KUID} is closer in terms of XOR distance
     * to the given key than the other {@link KUID} is to the key.
     */
    public boolean isCloserTo(KUID key, KUID otherId) {
        return xor(key).compareTo(key.xor(otherId)) < 0;
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

    /**
     * Returns the {@link KUID}'s value as an {@link BigInteger}
     */
    public BigInteger toBigInteger() {
        return new BigInteger(1 /* unsigned */, key);
    }
    
    /**
     * Returns the {@link KUID}'s value as a Base 16 (hex) encoded String.
     */
    public String toHexString() {
        return CodingUtils.encodeBase16(key);
    }
    
    @Override
    public String toString() {
        return toHexString();
    }
    
    public static KeyAnalyzer<KUID> createKeyAnalyzer(KUID key) {
        if (key == null) {
            throw new NullArgumentException("key");
        }
        
        return createKeyAnalyzer(key.lengthInBits());
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
    
    public static void main(String[] args) {
        int bit = 0x100;
        
        bit >>>= 0;
        System.out.println(Integer.toBinaryString(bit));
    }
}
