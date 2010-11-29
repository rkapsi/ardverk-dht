package com.ardverk.dht.storage;

import java.security.MessageDigest;

import org.ardverk.security.MessageDigestUtils;

public class ValueUtils {

    private ValueUtils() {}
    
    public static byte[] createId(byte[] value) {
        MessageDigest md = MessageDigestUtils.createCRC32();
        return md.digest(value);
    }
}
