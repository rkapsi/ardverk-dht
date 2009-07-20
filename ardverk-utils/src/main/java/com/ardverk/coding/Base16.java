package com.ardverk.coding;

import java.io.Serializable;

public class Base16 extends AbstractBaseCoder implements Serializable {
    
    private static final long serialVersionUID = 485864896492653810L;

    private static final char[] HEX = {
        '0', '1', '2', '3', '4', '5',  '6', '7', '8', '9', 
        'a', 'b', 'c', 'd', 'e', 'f'
    };
    
    public static final Base16 CODER = new Base16();
    
    private Base16() {}
    
    public byte[] encode(byte[] data, int offset, int length) {
        
        if (data == null) {
            throw new NullPointerException("data");
        }
        
        if (offset < 0 || length < 0 || (offset+length) > data.length) {
            throw new IllegalArgumentException("offset=" + offset + ", length=" 
                    + length + ", data.length=" + data.length);
        }
        
        // For each input byte we will produce two output bytes!
        byte[] output = new byte[length * 2];
        int end = offset + length;
        byte value = 0;
        
        for (int i = offset, j = 0; i < end; i++) {
            value = data[i];
            output[j++] = (byte)(HEX[(value >>> 4) & 0xF]);
            output[j++] = (byte)(HEX[(value      ) & 0xF]);
        }
        
        return output;
    }
    
    public byte[] decode(byte[] data, int offset, int length) {
        if (data == null) {
            throw new NullPointerException("data");
        }
        
        if (offset < 0 || length < 0 || (offset+length) > data.length) {
            throw new IllegalArgumentException("offset=" + offset + ", length=" 
                    + length + ", data.length=" + data.length);
        }
        
        byte[] output = new byte[(int)(length/2f + 0.5f)];
        
        int hi = 0, lo = 0;
        for (int i = 0, j = offset; i < output.length; i++) {
            if (i == 0 && (data.length % 2) != 0) {
                hi = 0;
                lo = parse(data[j++] & 0xFF);
            } else {
                hi = parse(data[j++] & 0xFF);
                lo = parse(data[j++] & 0xFF);
            }
            
            output[i] = (byte)(((hi & 0xF) << 4) | (lo & 0xF));
        }
        return output;
    }
    
    public static byte[] encodeBase16(byte[] data) {
        return CODER.encode(data);
    }
    
    public static byte[] encodeBase16(byte[] data, int offset, int length) {
        return CODER.encode(data, offset, length);
    }
    
    public static byte[] decodeBase16(byte[] data) {
        return CODER.decode(data);
    }
    
    public static byte[] decodeBase16(byte[] data, int offset, int length) {
        return CODER.decode(data, offset, length);
    }
    
    private static int parse(int c) {
        switch(c) {
            case '0': return 0;
            case '1': return 1;
            case '2': return 2;
            case '3': return 3;
            case '4': return 4;
            case '5': return 5;
            case '6': return 6;
            case '7': return 7;
            case '8': return 8;
            case '9': return 9;
            case 'a': return 10;
            case 'A': return 10;
            case 'b': return 11;
            case 'B': return 11;
            case 'c': return 12;
            case 'C': return 12;
            case 'd': return 13;
            case 'D': return 13;
            case 'e': return 14;
            case 'E': return 14;
            case 'f': return 15;
            case 'F': return 15;
            default: throw new NumberFormatException("c=" + (char)c);
        }
    }
}
