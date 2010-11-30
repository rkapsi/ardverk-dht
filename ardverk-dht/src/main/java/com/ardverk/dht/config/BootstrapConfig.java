package com.ardverk.dht.config;

public interface BootstrapConfig extends Config {

    public PingConfig getPingConfig();
    
    public void setPingConfig(PingConfig pingConfig);
    
    public LookupConfig getLookupConfig();
    
    public void setLookupConfig(LookupConfig lookupConfig);
}
