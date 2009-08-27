package com.ardverk.utils;

import java.io.UnsupportedEncodingException;

public class StringUtils {

    public static final String UTF_8 = "UTF-8";
    
    public static final String ISO_8859_1 = "ISO-8859-1";
    
    private StringUtils() {}
    
    public static String toString(byte[] data) {
        return toString(data, UTF_8);
    }
    
    public static String toString(byte[] data, String encoding) {
        try {
            return new String(data, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("encoding=" + encoding, e);
        }
    }
    
    public static byte[] getBytes(String data) {
        return getBytes(data, UTF_8);
    }
    
    public static byte[] getBytes(String data, String encoding) {
        try {
            return data.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("encoding=" + encoding, e);
        }
    }
}
