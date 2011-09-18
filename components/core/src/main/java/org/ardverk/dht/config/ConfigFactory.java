package org.ardverk.dht.config;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultConfigFactory.class)
public interface ConfigFactory {

    public PingConfig newPingConfig();
    
    public GetConfig newGetConfig();
    
    public PutConfig newPutConfig();
    
    public LookupConfig newLookupConfig();
    
    public BootstrapConfig newBootstrapConfig();
    
    public QuickenConfig newQuickenConfig();
}
