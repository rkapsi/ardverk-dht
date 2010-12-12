/*
 * Copyright 2009-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ardverk.dht.codec;

import java.io.IOException;
import java.net.SocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.ardverk.coding.CodingUtils;
import org.ardverk.security.SecurityUtils;

import com.ardverk.dht.message.Message;

/**
 * 
 */
public class CipherMessageCodec extends AbstractMessageCodec {
    
    private static final String ALGORITHM = "AES";
    
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private static final int DEFAULT_KEY_SIZE = 128;
    
    private static final int DEFAULT_INIT_VECTOR_SIZE = 16;
    
    private final MessageCodec codec;
    
    private final Cipher cipher;
    
    private final SecretKey secretKey;
    
    private final IvParameterSpec params;
    
    private final SecureRandom random;
    
    public CipherMessageCodec(MessageCodec codec, 
            String secretKey, String initVector) {
        this(codec, createSecretKey(secretKey), createInitVector(initVector));
    }
    
    public CipherMessageCodec(MessageCodec codec, 
            byte[] secretKey, byte[] initVector) {
        this(codec, createSecretKey(secretKey), createInitVector(initVector));
    }
    
    public CipherMessageCodec(MessageCodec codec, SecretKey secretKey, 
            IvParameterSpec params) {
        this(codec, secretKey, params, SecurityUtils.createSecureRandom());
    }
    
    public CipherMessageCodec(MessageCodec codec, SecretKey secretKey, 
            IvParameterSpec params, SecureRandom random) {
        this.codec = codec;
        this.secretKey = secretKey;
        this.params = params;
        this.random = random;
        
        try {
            this.cipher = Cipher.getInstance(TRANSFORMATION);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(TRANSFORMATION, e);
        } catch (NoSuchPaddingException e) {
            throw new IllegalArgumentException("NoSuchPaddingException", e);
        }
    }
    
    @Override
    public byte[] encode(Message message) throws IOException {
        return encrypt(codec.encode(message));
    }

    @Override
    public Message decode(SocketAddress src, byte[] data, int offset, int length)
            throws IOException {
        return codec.decode(src, decrypt(data, offset, length));
    }
    
    private byte[] encrypt(byte[] data) throws IOException {
        return doFinal(Cipher.ENCRYPT_MODE, data);
    }
    
    private byte[] decrypt(byte[] data, int offset, int length) throws IOException {
        return doFinal(Cipher.DECRYPT_MODE, data, offset, length);
    }
    
    private byte[] doFinal(int mode, byte[] data) throws IOException {
        return doFinal(mode, data, 0, data.length);
    }
    
    private synchronized byte[] doFinal(int mode, byte[] data, 
            int offset, int length) throws IOException {
        try {
            cipher.init(mode, secretKey, params, random);
            return cipher.doFinal(data, offset, length);
        } catch (InvalidKeyException e) {
            throw new IOException("InvalidKeyException", e);
        } catch (IllegalBlockSizeException e) {
            throw new IOException("IllegalBlockSizeException", e);
        } catch (BadPaddingException e) {
            throw new IOException("BadPaddingException", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IOException("InvalidAlgorithmParameterException", e);
        }
    }
    
    /**
     * Creates and returns an AES {@link SecretKey}.
     */
    public static SecretKey createSecretKey(int keySize) {
        try {
            KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM);
            generator.init(keySize);
            return generator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(ALGORITHM, e);
        }
    }
    
    /**
     * Parses and returns an AES {@link SecretKey}.
     */
    public static SecretKey createSecretKey(byte[] secretKey) {
        return new SecretKeySpec(secretKey, ALGORITHM);
    }
    
    /**
     * Parses and returns an AES {@link SecretKey} from a Base16 String.
     */
    public static SecretKey createSecretKey(String secretKey) {
        byte[] data = CodingUtils.decodeBase16(secretKey);
        return createSecretKey(data);
    }
    
    /**
     * Turns an AES {@link SecretKey} into a Base-16 encoded String.
     */
    public static String toString(SecretKey secretKey) {
        byte[] encoded = secretKey.getEncoded();
        return CodingUtils.encodeBase16(encoded);
    }
    
    /**
     * Creates and returns an {@link IvParameterSpec}.
     */
    public static IvParameterSpec createInitVector() {
        byte[] initVector = new byte[DEFAULT_INIT_VECTOR_SIZE];
        
        SecureRandom random = SecurityUtils.createSecureRandom();
        random.nextBytes(initVector);
        
        return createInitVector(initVector);
    }
    
    /**
     * Parses and returns an {@link IvParameterSpec}.
     */
    public static IvParameterSpec createInitVector(byte[] initVector) {
        return new IvParameterSpec(initVector);
    }
    
    /**
     * Parses and returns an {@link IvParameterSpec} from a Base16 String.
     */
    public static IvParameterSpec createInitVector(String initVector) {
        byte[] data = CodingUtils.decodeBase16(initVector);
        return createInitVector(data);
    }
    
    /**
     * Turns an {@link IvParameterSpec} into a Base16 encoded String.
     */
    public static String toString(IvParameterSpec iv) {
        return CodingUtils.encodeBase16(iv.getIV());
    }
    
    /**
     * Use this tool to generate a random {@link SecretKey} and 
     * {@link IvParameterSpec} for your application. 
     * 
     * Take the two Base16 (hex) encoded Strings and deploy them
     * with your application.
     */
    public static void main(String[] args) {
        int keySize = DEFAULT_KEY_SIZE;
        if (args.length != 0) {
            keySize = Integer.parseInt(args[0]);
        }
        
        System.out.println("Algorithm: " + ALGORITHM);
        System.out.println("Key Size: " + keySize 
                + ((keySize == DEFAULT_KEY_SIZE) ? " (default)" : ""));
        
        System.out.println("Secret Key: " + toString(createSecretKey(keySize)));
        System.out.println("Init Vector: " + toString(createInitVector()));
    }
}