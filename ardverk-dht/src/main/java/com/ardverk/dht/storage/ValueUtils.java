package com.ardverk.dht.storage;

import java.security.MessageDigest;
import java.util.concurrent.atomic.AtomicInteger;

import org.ardverk.security.MessageDigestUtils;

import com.ardverk.dht.KUID;

public class ValueUtils {

    private static final AtomicInteger ID = new AtomicInteger();
    
    private ValueUtils() {}
    
    public static int createId() {
        int value = 0;
        while ((value = ID.incrementAndGet()) == 0);
        return value;
    }
    
    public static byte[] createId(KUID key, byte[] value) {
        MessageDigest md = MessageDigestUtils.createSHA1();
        key.update(md);
        md.update(value);
        return md.digest();
    }
}
