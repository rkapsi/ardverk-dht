package com.ardverk.dht.message;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.ardverk.coding.Base16;
import org.ardverk.coding.CodingUtils;
import org.ardverk.security.SecurityUtils;
import org.ardverk.utils.StringUtils;

/**
 * An utility class to create and parse {@link SecretKey}s and 
 * {@link IvParameterSpec}s for the {@link CipherMessageCodec}.
 */
public class CipherUtils {

    private static final String ALGORITHM = "AES";
    
    private static final int DEFAULT_KEY_SIZE = 128;
    
    private static final int DEFAULT_INIT_VECTOR_SIZE = 16;
    
    private CipherUtils() {}
    
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
        byte[] data = Base16.decodeBase16(StringUtils.getBytes(secretKey));
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
        byte[] data = Base16.decodeBase16(StringUtils.getBytes(initVector));
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
