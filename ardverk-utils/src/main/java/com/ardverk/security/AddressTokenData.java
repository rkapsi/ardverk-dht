package com.ardverk.security;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.ardverk.net.NetworkUtils;


/**
 * 
 */
public class AddressTokenData extends AbstractTokenData {

    private static final int LENGTH = 8;
    
    private final byte[] data = new byte[LENGTH];
    
    public AddressTokenData(SocketAddress addr) {
        this (NetworkUtils.getAddress(addr), 
                NetworkUtils.getPort(addr));
    }
    
    public AddressTokenData(String addr, int port) 
            throws UnknownHostException {
        this (InetAddress.getByName(addr), port);
    }

    public AddressTokenData(InetAddress addr, int port) {
        this (addr.getAddress(), port);
    }
    
    public AddressTokenData(byte[] addr, int port) {
        
        data[0] = 0x00;
        data[1] = 0x00;
        
        int code = Arrays.hashCode(addr);
        data[2] = (byte)((code >>> 24) & 0xFF);
        data[3] = (byte)((code >>> 16) & 0xFF);
        data[4] = (byte)((code >>>  8) & 0xFF);
        data[5] = (byte)((code       ) & 0xFF);
        
        data[6] = (byte)((port >>> 8) & 0xFF);
        data[7] = (byte)((port      ) & 0xFF);
    }

    @Override
    public int length() {
        return data.length;
    }
    
    @Override
    public byte[] getBytes(byte[] dst, int offset) {
        System.arraycopy(data, 0, dst, offset, data.length);
        return dst;
    }
    
    @Override
    public int write(OutputStream out) throws IOException {
        out.write(data);
        return data.length;
    }
}
