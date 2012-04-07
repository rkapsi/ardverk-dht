package org.ardverk.dht.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Injector;

@Singleton
public class DefaultConfigFactory implements ConfigFactory {

  private final Injector injector;
  
  public DefaultConfigFactory() {
    this(null);
  }
  
  @Inject
  public DefaultConfigFactory(Injector injector) {
    this.injector = injector;
  }
  
  private <T> T newConfig(Class<T> type) {
    if (injector != null) {
      return injector.getInstance(type);
    }
    
    try {
      return type.newInstance();
    } catch (InstantiationException e) {
      throw new IllegalStateException("InstantiationException", e);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("IllegalAccessException", e);
    }
  }
  
  @Override
  public PingConfig newPingConfig() {
    return newConfig(PingConfig.class);
  }

  @Override
  public ValueConfig newGetConfig() {
    return newConfig(ValueConfig.class);
  }
  
  @Override
  public PutConfig newPutConfig() {
    return newConfig(PutConfig.class);
  }

  @Override
  public NodeConfig newLookupConfig() {
    return newConfig(NodeConfig.class);
  }

  @Override
  public BootstrapConfig newBootstrapConfig() {
    return newConfig(BootstrapConfig.class);
  }
  
  @Override
  public QuickenConfig newQuickenConfig() {
    return newConfig(QuickenConfig.class);
  }
}
