package org.ardverk.dht;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class Factory {
  
  private static final int SHA1 = 20;
  
  public static Factory sha1() {
    return new Factory(SHA1);
  }
  
  private final int keySize;
  
  private Factory(int keySize) {
    this.keySize = keySize;
  }
  
  public DHT newDHT(int port, Module... modules) {
    return newDHT(new InetSocketAddress(port), modules);
  }
  
  public DHT newDHT(String host, int port, Module... modules) {
    return newDHT(InetSocketAddress.createUnresolved(host, port), modules);
  }
  
  public DHT newDHT(SocketAddress address, Module... modules) {
    return createInjector(address, modules).getInstance(DHT.class);
  }
  
  public Injector createInjector(SocketAddress address, Module... modules) {
    List<Module> m = new ArrayList<>();
    m.add(new ArdverkModule(keySize, address));
    m.addAll(Arrays.asList(modules));
    
    return Guice.createInjector(m);
  }
}
