/*
 * Copyright 2009-2012 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.dht;

import java.io.Closeable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.DHTProcess;
import org.ardverk.dht.concurrent.ExecutorKey;
import org.ardverk.dht.config.Config;
import org.ardverk.dht.config.PingConfig;
import org.ardverk.dht.entity.PingEntity;
import org.ardverk.dht.routing.Identity;
import org.ardverk.dht.routing.RouteTable;
import org.ardverk.dht.storage.Datastore;
import org.ardverk.io.IoUtils;


/**
 * An abstract implementation of {@link DHT}.
 */
abstract class AbstractDHT implements DHT, Closeable {

  protected final RouteTable routeTable;

  protected final Datastore datastore;
  
  protected final FutureManager futureManager;
  
  public AbstractDHT(RouteTable routeTable, Datastore datastore, FutureManager futureManager) {
    this.routeTable = routeTable;
    this.datastore = datastore;
    this.futureManager = futureManager;
  }
  
  @Override
  public RouteTable getRouteTable() {
    return routeTable;
  }
  
  @Override
  public Datastore getDatabase() {
    return datastore;
  }
  
  @Override
  public void close() {
    IoUtils.close(futureManager);
  }
  
  @Override
  public Identity getIdentity() {
    return routeTable.getIdentity();
  }
  
  @Override
  public DHTFuture<PingEntity> ping(InetAddress address, 
      int port, PingConfig... config) {
    return ping(new InetSocketAddress(address, port), config);
  }
  
  @Override
  public DHTFuture<PingEntity> ping(String address, 
      int port, PingConfig... config) {
    return ping(new InetSocketAddress(address, port), config);
  }
  
  @Override
  public <V> DHTFuture<V> submit(DHTProcess<V> process, Config config) {
    return futureManager.submit(process, config);
  }

  @Override
  public <V> DHTFuture<V> submit(ExecutorKey executorKey,
      DHTProcess<V> process, long timeout, TimeUnit unit) {
    return futureManager.submit(executorKey, process, timeout, unit);
  }
  
  @Override
  public String toString() {
    return getIdentity().toString();
  }
}