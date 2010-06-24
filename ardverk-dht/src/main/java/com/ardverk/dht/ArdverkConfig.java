package com.ardverk.dht;

import java.util.Properties;

public class ArdverkConfig {
    
    private static final int DEFAULT_K = 20;
    
    private volatile int k;
    
    private volatile int r;
    
    private volatile int w;
    
    public ArdverkConfig() {
        this (System.getProperties());
    }
    
    public ArdverkConfig(Properties props) {
        this.k = getInteger(props, "K", DEFAULT_K);
        this.r = getInteger(props, "R", Math.max(k-1, 1));
        this.w = getInteger(props, "W", Math.max(k-1, 1));
    }
    
    public int getK() {
        return k;
    }
    
    public void setK(int k) {
        this.k = k;
    }
    
    public int getR() {
        return r;
    }
    
    public void setR(int r) {
        this.r = r;
    }
    
    public int getW() {
        return w;
    }
    
    public void setW(int w) {
        this.w = w;
    }
    
    private static String key(String suffix) {
        Package pkg = ArdverkConfig.class.getPackage();
        return pkg.getName() + "." + suffix;
    }
    
    
    private static int getInteger(Properties props, String suffix) {
        return getInteger(props, suffix, 0, false);
    }
    
    private static int getInteger(Properties props, String suffix, int defaultValue) {
        return getInteger(props, suffix, defaultValue, true);
    }
    
    private static int getInteger(Properties props, String suffix, 
            int defaultValue, boolean hasDefault) {
        String value = props.getProperty(key(suffix));
        if (value != null) {
            return Integer.parseInt(value);
        }
        
        if (hasDefault) {
            return defaultValue;
        }
        
        throw new IllegalArgumentException("suffix=" + suffix);
    }
    
    private static long getLong(Properties props, String suffix) {
        return getLong(props, suffix, 0, false);
    }
    
    private static long getLong(Properties props, String suffix, long defaultValue) {
        return getLong(props, suffix, defaultValue, true);
    }
    
    private static long getLong(Properties props, String suffix, 
            long defaultValue, boolean hasDefault) {
        String value = props.getProperty(key(suffix));
        if (value != null) {
            return Long.parseLong(value);
        }
        
        if (hasDefault) {
            return defaultValue;
        }
        
        throw new IllegalArgumentException("suffix=" + suffix);
    }
}
