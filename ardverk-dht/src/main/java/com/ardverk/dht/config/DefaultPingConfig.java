package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

public class DefaultPingConfig extends DefaultConfig 
        implements PingConfig {
    
    public DefaultPingConfig() {
        super(10L, TimeUnit.SECONDS);
    }
}