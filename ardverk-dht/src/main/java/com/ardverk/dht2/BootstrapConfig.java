package com.ardverk.dht2;

public interface BootstrapConfig extends Config {

    public PingConfig getPingConfig();
    
    public LookupConfig getLookupConfig();
    
    public boolean isRefreshBuckets();
}
