package org.ardverk.dht.config;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton 
public class ConfigProvider {
    
    private final ConfigFactory cf;
    
    @Inject
    public ConfigProvider(ConfigFactory cf) {
        this.cf = cf;
    }
    
    private static boolean isNull(Config... config) {
        return config == null || config.length == 0 || config[0] == null;
    }
    
    public BootstrapConfig get(BootstrapConfig... config) {
        return isNull(config) ? cf.newBootstrapConfig() : config[0];
    }
    
    public PingConfig get(PingConfig... config) {
        return isNull(config) ? cf.newPingConfig() : config[0];
    }
    
    public GetConfig get(GetConfig... config) {
        return isNull(config) ? cf.newGetConfig() : config[0];
    }
    
    public PutConfig get(PutConfig... config) {
        return isNull(config) ? cf.newPutConfig() : config[0];
    }
    
    public QuickenConfig get(QuickenConfig... config) {
        return isNull(config) ? cf.newQuickenConfig() : config[0];
    }
    
    public LookupConfig get(LookupConfig... config) {
        return isNull(config) ? cf.newLookupConfig() : config[0];
    }
}