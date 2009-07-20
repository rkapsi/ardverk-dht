package com.ardverk.coding;

import com.ardverk.utils.StringUtils;

public class CodingUtils {

    private CodingUtils() {}
    
    public static String encodeBase16(String data) {
        return encodeBase16(StringUtils.getBytes(data));
    }
    
    public static String encodeBase16(byte[] data) {
        return encodeBase16(data, 0, data.length);
    }
    
    public static String encodeBase16(byte[] data, int offset, int length) {
        return StringUtils.toString(Base16.encodeBase16(data, offset, length));
    }
    
    public static byte[] decodeBase16(String data) {
        return decodeBase16(StringUtils.getBytes(data));
    }
    
    public static byte[] decodeBase16(byte[] data) {
        return Base16.decodeBase16(data);
    }
}
