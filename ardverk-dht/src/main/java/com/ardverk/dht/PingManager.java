/*
 * Copyright 2009-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ardverk.dht;

import java.net.SocketAddress;

import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.PingConfig;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.io.MessageDispatcher;
import com.ardverk.dht.io.PingResponseHandler;
import com.ardverk.dht.routing.Contact;

public class PingManager {

    private final DHT dht;
    
    private final MessageDispatcher messageDispatcher;
    
    PingManager(DHT dht, MessageDispatcher messageDispatcher) {
        this.dht = dht;
        this.messageDispatcher = messageDispatcher;
    }
    
    public ArdverkFuture<PingEntity> ping(Contact contact, PingConfig config) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, contact, config);
        return dht.submit(process, config);
    }
    
    public ArdverkFuture<PingEntity> ping(
            SocketAddress dst, PingConfig config) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, dst, config);
        return dht.submit(process, config);
    }
}
