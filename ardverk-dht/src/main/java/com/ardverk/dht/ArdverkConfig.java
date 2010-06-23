package com.ardverk.dht;

import java.util.Properties;

import org.ardverk.lang.NumberUtils;

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
        this.r = getInteger(props, "R", k-1);
        this.w = getInteger(props, "W", k-1);
        
        
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
        return NumberUtils.getInteger(props.getProperty(key(suffix)));
    }
    
    private static int getInteger(Properties props, String suffix, int defaultValue) {
        return NumberUtils.getInteger(props.getProperty(key(suffix), 
                Integer.toString(defaultValue)));
    }
}
