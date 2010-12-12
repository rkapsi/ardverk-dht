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

import java.net.InetAddress;
import java.net.SocketAddress;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.BootstrapConfig;
import com.ardverk.dht.entity.BootstrapEntity;
import com.ardverk.dht.routing.Contact;

public interface BootstrapService {

    /**
     * 
     */
    public ArdverkFuture<BootstrapEntity> bootstrap(
            String host, int port, BootstrapConfig config);
    
    /**
     * 
     */
    public ArdverkFuture<BootstrapEntity> bootstrap(
            InetAddress address, int port, BootstrapConfig config);
    
    /**
     * 
     */
    public ArdverkFuture<BootstrapEntity> bootstrap(
            SocketAddress address, BootstrapConfig config);
    
    /**
     * 
     */
    public ArdverkFuture<BootstrapEntity> bootstrap(
            Contact contact, BootstrapConfig config);
}