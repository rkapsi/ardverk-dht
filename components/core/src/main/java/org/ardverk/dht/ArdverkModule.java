package org.ardverk.dht;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.ardverk.dht.routing.Identity;
import org.ardverk.dht.storage.Datastore;
import org.ardverk.dht.storage.TransientDatastore;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class ArdverkModule extends AbstractModule {

  private final KUID contactId;

  private final SocketAddress address;
  
  public ArdverkModule(int keySize, SocketAddress address) {
    this(KUID.createRandom(keySize), address);
  }

  public ArdverkModule(KUID contactId, SocketAddress address) {
    this.contactId = contactId;
    this.address = address;
  }

  @Override
  protected void configure() {
  }

  @Provides @Singleton
  Identity getIdentity() {
    return new Identity(contactId, address);
  }

  @Provides @Singleton
  Datastore getDatastore() {
    return new TransientDatastore(30L, TimeUnit.MINUTES);
  }
}
