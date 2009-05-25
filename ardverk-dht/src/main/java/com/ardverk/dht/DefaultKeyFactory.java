package com.ardverk.dht;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public class DefaultKeyFactory implements KeyFactory {

    private static final long serialVersionUID = -3573934494417059105L;

    private static final Random GENEREATOR = new SecureRandom();

    private final int lengthInBits;

    private final int bitMask;

    private final int length;
    
    private final KUID min;

    private final KUID max;
    
    public DefaultKeyFactory(int lengthInBits) {
        if (lengthInBits <= 0) {
            throw new IllegalArgumentException("lengthInBits=" + lengthInBits);
        }
        
        this.lengthInBits = lengthInBits;

        this.length = Math.max(1, Math.round(lengthInBits / 8f));
        byte[] minKey = new byte[length];
        byte[] maxKey = new byte[length];
        Arrays.fill(maxKey, (byte) 0xFF);

        int remaining = lengthInBits % 8;
        int bitMask = 0x00;
        for (int i = 0; i < remaining; i++) {
            bitMask |= (0x80 >>> i);
        }
        
        if (bitMask == 0x00) {
            bitMask = KUID.NO_BIT_MASK;
        }
        
        this.bitMask = bitMask;
        
        this.min = createKey(minKey, false);
        this.max = createKey(maxKey, false);
    }

    @Override
    public KUID createKey(byte[] key) {
        return createKey(key, true);
    }
    
    protected KUID createKey(byte[] key, boolean copy) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        
        if (key.length != length()) {
            throw new IllegalArgumentException("key.length=" + key.length);
        }
        
        if (copy) {
            key = key.clone();
        }
        
        return new KUID(key, bitMask(), lengthInBits());
    }

    @Override
    public KUID createKey(InputStream in) throws IOException {
        if (in == null) {
            throw new NullPointerException("in");
        }
        
        byte[] key = new byte[length()];
        int len = -1;
        int offset = 0;
        while ((len = in.read(key, offset, key.length - offset)) != -1) {
            offset += len;
        }

        if (offset != key.length) {
            throw new EOFException();
        }

        return createKey(key, false);
    }
    
    @Override
    public KUID createKey(ByteBuffer in) {
        if (in == null) {
            throw new NullPointerException("in");
        }
        
        byte[] key = new byte[length()];
        in.get(key);
        
        return createKey(key, false);
    }
    
    @Override
    public KUID createKey(String value, int radix) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        
        if (radix <= 0) {
            throw new IllegalArgumentException("radix=" + radix);
        }
        
        return createKey(new BigInteger(value, radix));
    }
    
    @Override
    public KUID createKey(BigInteger value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        
        int lengthInBits = lengthInBits();
        if (value.bitCount() > lengthInBits 
                || value.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("value=" + value);
        }
        
        if (bitMask() != KUID.NO_BIT_MASK) {
            int bits = (lengthInBits / 8);
            value = value.shiftLeft(bits);
        }
        
        byte[] bytes = value.toByteArray();
        
        byte[] key = new byte[length()];
        System.arraycopy(bytes, 0, key, key.length-bytes.length, bytes.length);
        
        return createKey(key, false);
    }

    @Override
    public KUID createRandomKey() {
        byte[] key = new byte[length()];
        GENEREATOR.nextBytes(key);
        return createKey(key, false);
    }

    @Override
    public int bitMask() {
        return bitMask;
    }

    @Override
    public int lengthInBits() {
        return lengthInBits;
    }
    
    @Override
    public int length() {
        return length;
    }
    
    @Override
    public KUID min() {
        return min;
    }
    
    @Override
    public KUID max() {
        return max;
    }
}
