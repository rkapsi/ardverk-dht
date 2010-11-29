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
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.ardverk.security.SecurityUtils;

import com.ardverk.dht.message.Message;

/**
 * 
 */
public class CipherMessageCodec extends AbstractMessageCodec {
    
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
        
    private final MessageCodec codec;
    
    private final Cipher cipher;
    
    private final SecretKey secretKey;
    
    private final IvParameterSpec params;
    
    private final SecureRandom random;
    
    public CipherMessageCodec(MessageCodec codec, 
            String secretKey, String initVector) {
        this(codec, CipherUtils.createSecretKey(secretKey), 
                CipherUtils.createInitVector(initVector));
    }
    
    public CipherMessageCodec(MessageCodec codec, 
            byte[] secretKey, byte[] initVector) {
        this(codec, CipherUtils.createSecretKey(secretKey), 
                CipherUtils.createInitVector(initVector));
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
}
