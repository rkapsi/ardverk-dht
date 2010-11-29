package com.ardverk.dht.utils;

import java.util.Properties;

import org.ardverk.lang.NumberUtils;

public class ArdverkProperties extends Properties {
    
    private static final long serialVersionUID = 7504266887232428200L;

    public ArdverkProperties() {
        super();
    }

    public ArdverkProperties(Properties defaults) {
        super(defaults);
    }
    
    public int getInteger(String key) {
        return NumberUtils.getInteger(getProperty(key));
    }
    
    public int getInteger(String key, int defaultValue) {
        return NumberUtils.getInteger(getProperty(key, 
                Integer.toString(defaultValue)));
    }
}
