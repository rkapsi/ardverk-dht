package com.ardverk.dht;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;

import org.ardverk.coding.CodingUtils;
import org.ardverk.collection.Key;
import org.ardverk.collection.KeyAnalyzer;
import org.ardverk.io.Writable;
import org.ardverk.lang.NullArgumentException;
import org.ardverk.security.SecurityUtils;

import com.ardverk.dht.lang.Identifier;
import com.ardverk.dht.lang.Negation;
import com.ardverk.dht.lang.Xor;

/**
 * Kademlia Unique Identifier ({@link KUID}) 
 */
public class KUID implements Identifier, Key<KUID>, Xor<KUID>, Negation<KUID>, 
        Writable, Serializable, Comparable<KUID>, Cloneable {

    private static final long serialVersionUID = -4611363711131603626L;
    
    private static final Random GENERATOR 
        = SecurityUtils.createSecureRandom();
    
    private static final int MSB = 1 << Byte.SIZE-1;
    
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
    
    public static KUID createWithPrefix(KUID prefix, int bits) {
        // 1) Create a random KUID of the same length
        byte[] dst = new byte[prefix.length()];
        GENERATOR.nextBytes(dst);
        
        // 2) Overwrite the prefix bytes
        ++bits;
        int length = bits/8;
        System.arraycopy(prefix.key, 0, dst, 0, length);
        
        // 3) Overwrite the remaining bits
        int bitsToCopy = bits % 8;
        if (bitsToCopy != 0) {
            // Mask has the low-order (8-bits) bits set
            int mask = (1 << (8-bitsToCopy)) - 1;
            int prefixByte = prefix.key[length];
            int randByte   = dst[length];
            dst[length] = (byte) ((prefixByte & ~mask) | (randByte & mask));
        }
        
        return create(dst);
    }
    
    private final byte[] key;
    
    private final int hashCode;
    
    private KUID(byte[] key) {
        if (key.length == 0) {
            throw new IllegalArgumentException(
                    "key.length=" + key.length);
        }
        
        this.key = key;
        this.hashCode = Arrays.hashCode(key);
    }
    
    @Override
    public KUID getId() {
        return this;
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
     * Calls {@link MessageDigest#update(byte[])} with the {@link KUID}'s bytes.
     */
    public void update(MessageDigest md) {
        md.update(key);
    }
    
    /**
     * Returns the length of the {@link KUID} in bytes.
     */
    public int length() {
        return key.length;
    }
    
    @Override
    public int lengthInBits() {
        return key.length * Byte.SIZE;
    }
    
    @Override
    public boolean isBitSet(int bitIndex) {
        int index = (int)(bitIndex / Byte.SIZE);
        int bit = (int)(bitIndex % Byte.SIZE);
        return (key[index] & mask(bit)) != 0;
    }
    
    @Override
    public int bitIndex(KUID otherKey) {
        int lengthInBits = lengthInBits();
        if (lengthInBits() != otherKey.lengthInBits()) {
            throw new IllegalArgumentException("otherKey=" + otherKey);
        }
        
        boolean allNull = true;
        for (int i = 0; i < lengthInBits; i++) {
            boolean value = isBitSet(i);
                
            if (value) {
                allNull = false;
            }
            
            boolean otherValue = otherKey.isBitSet(i);
            if (value != otherValue) {
                return i;
            }
        }
        
        if (allNull) {
            return KeyAnalyzer.NULL_BIT_KEY;
        }
        
        return KeyAnalyzer.EQUAL_BIT_KEY;
    }

    @Override
    public boolean isPrefixedBy(KUID prefix) {
        if (key.length < prefix.key.length) {
            return false;
        }
        
        for (int i = 0; i < prefix.key.length; i++) {
            if (key[i] != prefix.key[i]) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public KUID xor(KUID otherId) {
        if (!isCompatible(otherId)) {
            throw new IllegalArgumentException("otherId=" + otherId);
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
        return set(bitIndex, !isBitSet(bitIndex));
    }
    
    private KUID set(int bitIndex, boolean on) {
        int lengthInBits = lengthInBits();
        
        if (bitIndex < 0 || lengthInBits < bitIndex) {
            throw new IllegalArgumentException("bitIndex=" + bitIndex);
        }
        
        int index = (int)(bitIndex / Byte.SIZE);
        int bit = (int)(bitIndex % Byte.SIZE);
        
        int mask = mask(bit);
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
     * Returns {@code true} if all bits of the {@link KUID} are zero
     */
    public boolean isMin() {
        return compare((byte)0x00);
    }
    
    /**
     * Returns {@code true} if all bits of the {@link KUID} are one
     */
    public boolean isMax() {
        return compare((byte)0xFF);
    }
    
    /**
     * Returns {@code true} if the {@link KUID}'s bytes have 
     * all the given expected value.
     */
    private boolean compare(byte expected) {
        for (int i = 0; i < key.length; i++) {
            if (expected != key[i]) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Returns {@code true} if this {@link KUID} is closer in terms of XOR 
     * distance to the given key than the other {@link KUID} is to the key.
     */
    public boolean isCloserTo(KUID key, KUID otherId) {
        return compareTo(key, otherId) < 0;
    }
    
    /**
     * 
     */
    public int compareTo(KUID key, KUID otherId) {
        return xor(key).compareTo(otherId.xor(key));
    }
    
    @Override
    public int compareTo(KUID otherId) {
        int length = length();
        if (length != otherId.length()) {
            throw new IllegalArgumentException("otherId=" + otherId);
        }
        
        for (int i = 0; i < length; i++) {
            int diff = (int)(key[i] & 0xFF) - (int)(otherId.key[i] & 0xFF);
            if (diff != 0) {
                return diff;
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

        KUID otherId = (KUID) obj;
        return Arrays.equals(key, otherId.key);
    }

    @Override
    public KUID clone() {
        return this;
    }
    
    @Override
    public int write(OutputStream out) throws IOException {
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
    
    /**
     * Returns the {@link KUID}'s value as a Base 2 (bin) encoded String.
     */
    public String toBinString() {
        return CodingUtils.encodeBase2(key);
    }
    
    @Override
    public String toString() {
        return toHexString();
        //return toBinString();
    }
    
    /**
     * Returns a bit mask where the given bit is set
     */
    private static int mask(int bit) {
        return MSB >>> bit;
    }
}
