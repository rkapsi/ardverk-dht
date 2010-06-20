package com.ardverk.dht.storage;

import java.security.MessageDigest;

import org.ardverk.security.MessageDigestUtils;

import com.ardverk.dht.KUID;

public class ValueUtils {

    private ValueUtils() {}
    
    public static byte[] createId(KUID key, byte[] value) {
        MessageDigest md = MessageDigestUtils.createCRC32();
        key.update(md);
        md.update(value);
        return md.digest();
    }
}
