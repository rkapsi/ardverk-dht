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
  
  public BootstrapConfig get(BootstrapConfig config) {
    return config != null ? cf.newBootstrapConfig() : config;
  }
  
  public PingConfig get(PingConfig config) {
    return config != null ? cf.newPingConfig() : config;
  }
  
  public ValueConfig get(ValueConfig config) {
    return config != null ? cf.newGetConfig() : config;
  }
  
  public PutConfig get(PutConfig config) {
    return config != null ? cf.newPutConfig() : config;
  }
  
  public QuickenConfig get(QuickenConfig config) {
    return config != null ? cf.newQuickenConfig() : config;
  }
  
  public NodeConfig get(NodeConfig config) {
    return config != null ? cf.newLookupConfig() : config;
  }
}