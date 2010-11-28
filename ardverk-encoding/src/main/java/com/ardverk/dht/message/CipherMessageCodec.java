package com.ardverk.dht.message;

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
import javax.crypto.spec.SecretKeySpec;

import org.ardverk.security.SecurityUtils;

public class CipherMessageCodec extends AbstractMessageCodec {

    private static final String ALGORITHM = "AES";
    
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
        
    private final MessageCodec codec;
    
    private final SecretKey secretKey;
    
    private final IvParameterSpec params;
    
    private final SecureRandom random;
    
    public CipherMessageCodec(MessageCodec codec, byte[] secretKey, byte[] iv) {
        this(codec, new SecretKeySpec(secretKey, ALGORITHM), 
                new IvParameterSpec(iv));
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
    
    private byte[] doFinal(int mode, byte[] data, 
            int offset, int length) throws IOException {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
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
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("NoSuchAlgorithmException", e);
        } catch (NoSuchPaddingException e) {
            throw new IOException("NoSuchPaddingException", e);
        }
    }
}
