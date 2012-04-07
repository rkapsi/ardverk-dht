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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ardverk.collection.Iterables;
import org.ardverk.concurrent.AsyncCompletionService;
import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.FutureUtils;
import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.DHTValueFuture;
import org.ardverk.dht.config.ConfigProvider;
import org.ardverk.dht.config.NodeConfig;
import org.ardverk.dht.config.PingConfig;
import org.ardverk.dht.config.QuickenConfig;
import org.ardverk.dht.entity.NodeEntity;
import org.ardverk.dht.entity.PingEntity;
import org.ardverk.dht.entity.QuickenEntity;
import org.ardverk.dht.routing.Bucket;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.RouteTable;
import org.ardverk.dht.utils.IdentifierUtils;
import org.ardverk.lang.TimeStamp;


/**
 * The {@link QuickenManager} provides methods to keep 
 * the {@link RouteTable} fresh.
 */
@Singleton
public class QuickenManager {

  private final ConfigProvider configProvider;
  
  private final PingManager pingManager;
  
  private final LookupManager lookupManager;
  
  private final RouteTable routeTable;
  
  @Inject
  QuickenManager(PingManager pingManager, LookupManager lookupManager,
      RouteTable routeTable, ConfigProvider configProvider) {
    this.pingManager = pingManager;
    this.lookupManager = lookupManager;
    this.routeTable = routeTable;
    this.configProvider = configProvider;
  }
  
  public DHTFuture<QuickenEntity> quicken(QuickenConfig... config) {
    
    QuickenConfig cfg = configProvider.get(config);
    
    TimeStamp creationTime = TimeStamp.now();
    
    List<DHTFuture<PingEntity>> pingFutures 
      = new ArrayList<DHTFuture<PingEntity>>();
    
    List<DHTFuture<NodeEntity>> lookupFutures 
      = new ArrayList<DHTFuture<NodeEntity>>();
    
    synchronized (routeTable) {
      int pingCount = (int)(routeTable.getK() * cfg.getPingCount());
      
      Contact localhost = routeTable.getIdentity();
      KUID localhostId = localhost.getId();

      if (0 < pingCount) {
        PingConfig pingConfig = cfg.getPingConfig();
        long contactTimeout = cfg.getContactTimeoutInMillis();
        
        Contact[] contacts = routeTable.select(localhostId, pingCount);
        for (Contact contact : contacts) {
          // Don't send PINGs to the localhost!
          if (contact.equals(localhost)) {
            continue;
          }
          
          if (contact.isTimeout(contactTimeout, TimeUnit.MILLISECONDS)) {
            DHTFuture<PingEntity> future 
              = pingManager.ping(contact, pingConfig);
            pingFutures.add(future);
          }
        }
      }
      
      NodeConfig lookupConfig = cfg.getLookupConfig();
      long bucketTimeout = cfg.getBucketTimeoutInMillis();
      
      Bucket[] buckets = routeTable.getBuckets();
      IdentifierUtils.byXor(buckets, localhostId);
      
      for (Bucket bucket : buckets) {
        if (bucket.contains(localhostId)) {
          continue;
        }
        
        TimeStamp timeStamp = bucket.getTimeStamp();
        if (timeStamp.getAgeInMillis() < bucketTimeout) {
          continue;
        }
        
        // Select a random ID with this prefix
        KUID randomId = KUID.createWithPrefix(
            bucket.getId(), bucket.getDepth());
        
        DHTFuture<NodeEntity> future 
          = lookupManager.lookup(randomId, lookupConfig);
        lookupFutures.add(future);
      }
    }
    
    @SuppressWarnings("unchecked")
    DHTFuture<PingEntity>[] pings 
      = pingFutures.toArray(new DHTFuture[0]);
    
    @SuppressWarnings("unchecked")
    DHTFuture<NodeEntity>[] lookups 
      = lookupFutures.toArray(new DHTFuture[0]);
    
    return new QuickenFuture(creationTime, pings, lookups);
  }
  
  public static class QuickenFuture extends DHTValueFuture<QuickenEntity> {
    
    private final TimeStamp timeStamp;
    
    private final DHTFuture<PingEntity>[] pingFutures;
    
    private final DHTFuture<NodeEntity>[] lookupFutures;
    
    private QuickenFuture(TimeStamp timeStamp, 
        DHTFuture<PingEntity>[] pingFutures, 
        DHTFuture<NodeEntity>[] lookupFutures) {
      this.timeStamp = timeStamp;
      this.pingFutures = pingFutures;
      this.lookupFutures = lookupFutures;
      
      @SuppressWarnings("unchecked")
      Iterable<? extends AsyncFuture<?>> futures 
          = Iterables.fromIterables(
              Arrays.asList(pingFutures), 
              Arrays.asList(lookupFutures));
      
      AsyncFuture<Void> complete = AsyncCompletionService.createVoid(futures);
      complete.addAsyncFutureListener(new AsyncFutureListener<Void>() {
        @Override
        public void operationComplete(AsyncFuture<Void> future) {
          complete();   
        }
      });
    }
    
    public DHTFuture<PingEntity>[] getPingFutures() {
      return pingFutures;
    }

    public DHTFuture<NodeEntity>[] getLookupFutures() {
      return lookupFutures;
    }
    
    @Override
    protected void done() {
      super.done();
      
      FutureUtils.cancelAll(pingFutures, true);
      FutureUtils.cancelAll(lookupFutures, true);
    }
    
    private void complete() {
      long time = timeStamp.getAgeInMillis();
      setValue(new QuickenEntity(pingFutures, lookupFutures, 
          time, TimeUnit.MILLISECONDS));
    }
  }
}