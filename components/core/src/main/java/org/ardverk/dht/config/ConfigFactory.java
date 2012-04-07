package org.ardverk.dht.config;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultConfigFactory.class)
public interface ConfigFactory {

  public PingConfig newPingConfig();
  
  public ValueConfig newGetConfig();
  
  public PutConfig newPutConfig();
  
  public NodeConfig newLookupConfig();
  
  public BootstrapConfig newBootstrapConfig();
  
  public QuickenConfig newQuickenConfig();
}
