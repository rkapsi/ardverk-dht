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

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;

import org.ardverk.io.Bindable;

import com.ardverk.dht.io.transport.Transport;

/**
 * The {@link NetworkService} binds the {@link DHT} 
 * to a {@link Transport} layer.
 */
public interface NetworkService extends Bindable<Transport> {

    /**
     * Binds the {@link DHT} to the given port.
     */
    public void bind(int port) throws IOException;
    
    /**
     * Binds the {@link DHT} to the given host-port.
     */
    public void bind(String host, int port) throws IOException;
    
    /**
     * Binds the {@link DHT} to the given {@link InetAddress} and port.
     */
    public void bind(InetAddress bindaddr, int port) throws IOException;
    
    /**
     * Binds the {@link DHT} to the given {@link SocketAddress}.
     */
    public void bind(SocketAddress address) throws IOException;
}
