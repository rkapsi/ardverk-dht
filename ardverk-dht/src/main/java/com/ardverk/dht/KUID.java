package com.ardverk.dht;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.lang.NullArgumentException;
import org.ardverk.collection.ByteArrayKeyAnalyzer;
import org.ardverk.collection.KeyAnalyzer;
import org.ardverk.io.Writable;

import com.ardverk.coding.CodingUtils;
import com.ardverk.dht.security.SecurityUtils;
import com.ardverk.lang.Negation;
import com.ardverk.lang.Xor;

/**
 * Kademlia Unique Identifier ({@link KUID}) 
 */
public class KUID implements Xor<KUID>, Negation<KUID>, 
        Writable, Serializable, Comparable<KUID>, Cloneable {

    private static final long serialVersionUID = -4611363711131603626L;
    
    private static final Random GENERATOR 
        = SecurityUtils.createSecureRandom();
    
    public static KUID createRandom(int length) {
        byte[] key = new byte[length];
        GENERATOR.nextBytes(key);
        return new KUID(key);
    }
    
    public static KUID createRandom(KUID otherId) {
        return createRandom(otherId.length());
    }
    
    public static KUID create(byte[] key) {
        return new KUID(key);
    }
    
    public static KUID create(byte[] key, int offset, int length) {
        byte[] copy = new byte[length];
        System.arraycopy(key, 0, copy, 0, copy.length);
        return new KUID(copy);
    }
    
    public static KUID create(BigInteger key) {
        return create(key.toByteArray());
    }
    
    public static KUID create(String key, int radix) {
        return create(new BigInteger(key, radix));
    }
    
    private final byte[] key;
    
    private final int hashCode;
    
    private KUID(byte[] key) {
        if (key == null) {
            throw new NullArgumentException("key");
        }
        
        if (key.length == 0) {
            throw new IllegalArgumentException(
                    "key.length=" + key.length);
        }
        
        this.key = key;
        this.hashCode = Arrays.hashCode(key);
    }

    /**
     * Returns {@code true} if the given {@link KUID} is compatible with
     * this {@link KUID}.
     */
    public boolean isCompatible(KUID otherId) {
        return otherId != null && length() == otherId.length();
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
        return key.length * Byte.SIZE;
    }
    
    @Override
    public KUID xor(KUID otherId) {
        if (otherId == null) {
            throw new NullArgumentException("otherId");
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
        if (bitIndex < 0 || lengthInBits() < bitIndex) {
            throw new IllegalArgumentException("bitIndex=" + bitIndex);
        }
        
        int index = (int)(bitIndex / Byte.SIZE);
        int bit = (int)(bitIndex % Byte.SIZE);
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
        
        if (bitIndex < 0 || lengthInBits < bitIndex) {
            throw new IllegalArgumentException("bitIndex=" + bitIndex);
        }
        
        int index = (int)(bitIndex / Byte.SIZE);
        int bit = (int)(bitIndex % Byte.SIZE);
        
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
    
    public int commonPrefix(KUID otherId) {
        return commonPrefix(otherId, 0, lengthInBits());
    }
    
    public int commonPrefix(KUID otherId, int offsetInBits, int length) {
        if (otherId == null) {
            throw new NullArgumentException("otherId");
        }
        
        int lengthInBits = lengthInBits();
        if (offsetInBits < 0 || length < 0 
                || lengthInBits < (offsetInBits+length)) {
            throw new IllegalArgumentException(
                    "offsetInBits=" + offsetInBits + ", length=" + length);
        }
        
        if (lengthInBits != otherId.lengthInBits()) {
            throw new IllegalArgumentException("otherId=" + otherId);
        }
        
        if (otherId != this) {
            int index = (int)(offsetInBits / Byte.SIZE);
            int bit = offsetInBits % Byte.SIZE;
            
            int bitIndex = 0;
            for (int i = index; i < key.length && bitIndex < length; i++) {
                int value = (int)(key[i] ^ otherId.key[i]);
                
                // A short cut we can take...
                if (value == 0 && (bit == 0 || i != index) && i < (key.length-1)) {
                    bitIndex += Byte.SIZE;
                    continue;
                }
                
                for (int j = (i == index ? bit : 0); j < Byte.SIZE 
                        && bitIndex < length; j++) {
                    if ((value & (0x80 >>> j)) != 0) {
                        return offsetInBits + bitIndex;
                    }
                    
                    ++bitIndex;
                }
            }
        }
        
        return offsetInBits + length;
    }
    
    /**
     * Returns true if all bits of the {@link KUID} are zero
     */
    public boolean isMin() {
        int lengthInBits = lengthInBits();
        return compare(0x00, 0, lengthInBits) == lengthInBits;
    }
    
    /**
     * Returns true if all bits of the {@link KUID} are one
     */
    public boolean isMax() {
        int lengthInBits = lengthInBits();
        return compare(0xFF, 0, lengthInBits) == lengthInBits;
    }
    
    private int compare(int expected, int offsetInBits, int length) {
        
        int lengthInBits = lengthInBits();
        if (offsetInBits < 0 || length < 0 
                || lengthInBits < (offsetInBits + length)) {
            throw new IllegalArgumentException(
                    "offsetInBits=" + offsetInBits + ", length=" + length);
        }
        
        int index = (int)(offsetInBits / Byte.SIZE);
        int bit = offsetInBits % Byte.SIZE;
        
        int bitIndex = 0;
        for (int i = 0; i < key.length && bitIndex < length; i++) {
            int value = (key[i] & 0xFF) ^ expected;
            
            // A shortcut we can take...
            if (value == 0 && (bit == 0 || i != index)) {
                bitIndex += Byte.SIZE;
                continue;
            }
            
            for (int j = (i == index ? bit : 0); 
                    j < Byte.SIZE && bitIndex < length; j++) {
                
                if ((value & (0x80 >>> j)) != 0) {
                    return offsetInBits + bitIndex;
                }
                
                ++bitIndex;
            }
        }
        
        return offsetInBits + length;
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
        return compareTo(otherId, 0, lengthInBits());
    }

    public int compareTo(KUID otherId, int offsetInBits, int length) {
        if (otherId == null) {
            throw new NullArgumentException("otherId");
        }
        
        int lengthInBits = lengthInBits();
        if (offsetInBits < 0 || length < 0 
                || lengthInBits < (offsetInBits + length)) {
            throw new IllegalArgumentException(
                    "offsetInBits=" + offsetInBits + ", length=" + length);
        }
        
        if (otherId.lengthInBits() != lengthInBits) {
            throw new IllegalArgumentException();
        }
        
        if (otherId != this) {
            int index = (int)(offsetInBits / Byte.SIZE);
            int bit = offsetInBits % Byte.SIZE;
            
            int bitIndex = 0;
            int mask, diff;
            byte value1, value2;
            
            for (int i = index; i < key.length && bitIndex < length; i++) {
                
                value1 = key[i];
                value2 = otherId.key[i];
                
                // A shot cut we can take...
                if (value1 == value2 && (bit == 0 || i != index)) {
                    bitIndex += Byte.SIZE;
                    continue;
                }
                
                for (int j = (i == index ? bit : 0); 
                        j < Byte.SIZE && bitIndex < length; j++) {
                    mask = 0x80 >>> j;
                    diff = (value1 & mask) - (value2 & mask);
                    
                    if (diff != 0) {
                        return diff;
                    }
                    
                    ++bitIndex;
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
        return lengthInBits() == other.lengthInBits() 
                    && Arrays.equals(key, other.key);
    }

    @Override
    public KUID clone() {
        return this;
    }
    
    @Override
    public int write(OutputStream out) throws IOException {
        if (out == null) {
            throw new NullArgumentException("out");
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
}
