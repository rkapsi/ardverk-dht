/*
 * Copyright 2009-2011 Roger Kapsi
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

import java.net.SocketAddress;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.DHTProcess;
import org.ardverk.dht.config.ConfigProvider;
import org.ardverk.dht.config.PingConfig;
import org.ardverk.dht.entity.PingEntity;
import org.ardverk.dht.io.MessageDispatcher;
import org.ardverk.dht.io.PingResponseHandler;
import org.ardverk.dht.routing.Contact;


/**
 * The {@link PingManager} manages PINGs.
 */
@Singleton
public class PingManager {

    private final ConfigProvider configProvider;
    
    private final FutureManager futureManager;
    
    private final Provider<MessageDispatcher> messageDispatcher;
    
    @Inject
    PingManager(ConfigProvider configProvider,
            FutureManager futureManager, 
            Provider<MessageDispatcher> messageDispatcher) {
        
        this.configProvider = configProvider;
        this.futureManager = futureManager;
        this.messageDispatcher = messageDispatcher;
    }
    
    public DHTFuture<PingEntity> ping(Contact contact, PingConfig... config) {
        
        PingConfig cfg = configProvider.get(config);
        
        DHTProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, contact, cfg);
        return futureManager.submit(process, cfg);
    }
    
    public DHTFuture<PingEntity> ping(
            SocketAddress dst, PingConfig... config) {
        
        PingConfig cfg = configProvider.get(config);
        
        DHTProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, dst, cfg);
        return futureManager.submit(process, cfg);
    }
}