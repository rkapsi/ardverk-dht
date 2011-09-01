package org.ardverk.dht.config;

public class DefaultConfigFactory implements ConfigFactory {

    @Override
    public PingConfig newPingConfig() {
        return new DefaultPingConfig();
    }

    @Override
    public GetConfig newGetConfig() {
        return new DefaultGetConfig();
    }
    
    @Override
    public PutConfig newPutConfig() {
        return new DefaultPutConfig();
    }

    @Override
    public LookupConfig newLookupConfig() {
        return new DefaultLookupConfig();
    }

    @Override
    public BootstrapConfig newBootstrapConfig() {
        return new DefaultBootstrapConfig();
    }
    
    @Override
    public QuickenConfig newQuickenConfig() {
        return new DefaultQuickenConfig();
    }
}
