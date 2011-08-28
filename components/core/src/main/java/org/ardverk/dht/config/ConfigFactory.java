package org.ardverk.dht.config;

public interface ConfigFactory {

    public PingConfig newPingConfig();
    
    public GetConfig newGetConfig();
    
    public PutConfig newPutConfig();
    
    public LookupConfig newLookupConfig();
    
    public BootstrapConfig newBootstrapConfig();
    
    public QuickenConfig newQuickenConfig();
}
