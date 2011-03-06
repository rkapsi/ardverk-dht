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

import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.DHTProcess;
import org.ardverk.dht.config.PingConfig;
import org.ardverk.dht.entity.PingEntity;
import org.ardverk.dht.io.MessageDispatcher;
import org.ardverk.dht.io.PingResponseHandler;
import org.ardverk.dht.routing.Contact;


/**
 * The {@link PingManager} manages PINGs.
 */
public class PingManager {

    private final FutureService futureService;
    
    private final MessageDispatcher messageDispatcher;
    
    PingManager(FutureService futureService, 
            MessageDispatcher messageDispatcher) {
        this.futureService = futureService;
        this.messageDispatcher = messageDispatcher;
    }
    
    public DHTFuture<PingEntity> ping(Contact contact, PingConfig config) {
        DHTProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, contact, config);
        return futureService.submit(process, config);
    }
    
    public DHTFuture<PingEntity> ping(
            SocketAddress dst, PingConfig config) {
        DHTProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, dst, config);
        return futureService.submit(process, config);
    }
}