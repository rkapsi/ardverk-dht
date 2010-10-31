package com.ardverk.dht2;

import java.util.concurrent.TimeUnit;


public class DefaultBootstrapConfig extends AbstractConfig 
        implements BootstrapConfig {

    private volatile PingConfig pingConfig = new DefaultPingConfig();
    
    private volatile LookupConfig lookupConfig = new DefaultLookupConfig();
    
    @Override
    public PingConfig getPingConfig() {
        return pingConfig;
    }
    
    @Override
    public void setPingConfig(PingConfig pingConfig) {
        this.pingConfig = pingConfig;
    }

    @Override
    public LookupConfig getLookupConfig() {
        return lookupConfig;
    }
    
    @Override
    public void setLookupConfig(LookupConfig lookupConfig) {
        this.lookupConfig = lookupConfig;
    }

    @Override
    public void setTimeout(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getTimeout(TimeUnit unit) {
        return ConfigUtils.getTimeout(unit, pingConfig, lookupConfig);
    }
}
