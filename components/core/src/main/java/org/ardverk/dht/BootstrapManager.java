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

import java.net.InetAddress;
import java.net.SocketAddress;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.FutureUtils;
import org.ardverk.concurrent.ValueReference;
import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.DHTProcess;
import org.ardverk.dht.concurrent.NopProcess;
import org.ardverk.dht.config.BootstrapConfig;
import org.ardverk.dht.config.ConfigProvider;
import org.ardverk.dht.entity.BootstrapEntity;
import org.ardverk.dht.entity.NodeEntity;
import org.ardverk.dht.entity.PingEntity;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.Identity;
import org.ardverk.net.NetworkUtils;

/**
 * The {@link BootstrapManager} manages the bootstrap process.
 */
@Singleton
public class BootstrapManager {

  private final Identity localhost;
  
  private final ConfigProvider configProvider;
  
  private final FutureManager futureManager;
  
  private final PingManager pingManager;
  
  private final LookupManager lookupManager;
  
  @Inject
  BootstrapManager(Identity localhost,
      ConfigProvider configProvider,
      FutureManager futureManager, 
      PingManager pingManager, 
      LookupManager lookupManager) {
    
    this.localhost = localhost;
    this.configProvider = configProvider;
    this.futureManager = futureManager;
    this.pingManager = pingManager;
    this.lookupManager = lookupManager;
  }
  
  public DHTFuture<BootstrapEntity> bootstrap(
      String host, int port, BootstrapConfig... config) {
    return bootstrap(NetworkUtils.createResolved(host, port), config);
  }
  
  public DHTFuture<BootstrapEntity> bootstrap(
      InetAddress address, int port, BootstrapConfig... config) {
    return bootstrap(NetworkUtils.createResolved(address, port), config);
  }
  
  public DHTFuture<BootstrapEntity> bootstrap(
      Contact contact, BootstrapConfig... config) {
    return bootstrap(contact.getRemoteAddress(), config);
  }
  
  public DHTFuture<BootstrapEntity> bootstrap(
      SocketAddress address, BootstrapConfig... config) {
    
    BootstrapConfig cfg = configProvider.get(config);
    
    DHTFuture<PingEntity> pingFuture = pingManager.ping(
        address, cfg.getPingConfig());
    
    return bootstrap(pingFuture, cfg);
  }
  
  private DHTFuture<BootstrapEntity> bootstrap(
      final DHTFuture<PingEntity> pingFuture, 
      final BootstrapConfig config) {
    
    final Object lock = new Object();
    
    synchronized (lock) {
      DHTProcess<BootstrapEntity> process = NopProcess.create();
      final DHTFuture<BootstrapEntity> userFuture 
        = futureManager.submit(process, config);
      
      final ValueReference<DHTFuture<NodeEntity>> lookupFutureRef
        = new ValueReference<DHTFuture<NodeEntity>>();
      
      pingFuture.addAsyncFutureListener(new AsyncFutureListener<PingEntity>() {
        @Override
        public void operationComplete(AsyncFuture<PingEntity> future) {
          synchronized (lock) {
            try {
              if (!future.isCancelled()) {
                handlePingEntity(future.get());
              } else {
                handleCancelled();
              }
            } catch (Throwable t) {
              handleException(t);
            }
          }
        }
        
        private void handlePingEntity(final PingEntity pingEntity) {
          Contact[] contacts = new Contact[] { pingEntity.getContact() };
          
          KUID localhostId = localhost.getId();
          AsyncFuture<NodeEntity> lookupFuture 
            = lookupFutureRef.make(
                lookupManager.lookup(contacts, 
                  localhostId, config.getLookupConfig()));
          
          lookupFuture.addAsyncFutureListener(new AsyncFutureListener<NodeEntity>() {
            @Override
            public void operationComplete(AsyncFuture<NodeEntity> future) {
              synchronized (lock) {
                try {
                  if (!future.isCancelled()) {
                    handleNodeEntity(future.get());
                  } else {
                    handleCancelled();
                  }
                } catch (Throwable t) {
                  handleException(t);
                }
              }
            }
            
            private void handleNodeEntity(NodeEntity nodeEntity) {
              userFuture.setValue(new BootstrapEntity(
                  pingEntity, nodeEntity));
            }
          });
        }
        
        private void handleCancelled() {
          userFuture.cancel(true);
        }
        
        private void handleException(Throwable t) {
          userFuture.setException(t);
        }
      });
      
      userFuture.addAsyncFutureListener(new AsyncFutureListener<BootstrapEntity>() {
        @Override
        public void operationComplete(AsyncFuture<BootstrapEntity> future) {
          synchronized (lock) {
            FutureUtils.cancel(pingFuture, true);
            FutureUtils.cancel(lookupFutureRef, true);
          }
        }
      });
      
      userFuture.setAttachment(new Attachment(pingFuture, lookupFutureRef));
      return userFuture;
    }
  }
  
  public static class Attachment {
    
    private final DHTFuture<PingEntity> pingFuture;
    
    private final ValueReference<DHTFuture<NodeEntity>> lookupFutureRef;
    
    private Attachment(DHTFuture<PingEntity> pingFuture, 
        ValueReference<DHTFuture<NodeEntity>> lookupFutureRef) {
      this.pingFuture = pingFuture;
      this.lookupFutureRef = lookupFutureRef;
    }
    
    public DHTFuture<PingEntity> getPingFuture() {
      return pingFuture;
    }
    
    public DHTFuture<NodeEntity> getLookupFuture() {
      return lookupFutureRef.get();
    }
  }
}