package com.ardverk.dht2;

import java.util.concurrent.TimeUnit;

public class DefaultPingConfig extends DefaultConfig 
        implements PingConfig {
    
    public DefaultPingConfig() {
        super(10L, TimeUnit.SECONDS);
    }
}