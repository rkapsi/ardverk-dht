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

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.DHTProcess;
import org.ardverk.dht.concurrent.ExecutorKey;
import org.ardverk.dht.config.Config;
import org.ardverk.dht.config.PingConfig;
import org.ardverk.dht.entity.PingEntity;
import org.ardverk.dht.routing.Localhost;


/**
 * An abstract implementation of {@link DHT}.
 */
abstract class AbstractDHT implements DHT, Closeable {

    private final FutureManager futureManager = new FutureManager();
    
    @Override
    public void bind(int port) throws IOException {
        bind(new InetSocketAddress(port));
    }

    @Override
    public void bind(String host, int port) throws IOException {
        bind(new InetSocketAddress(host, port));
    }

    @Override
    public void bind(InetAddress bindaddr, int port) throws IOException {
        bind(new InetSocketAddress(bindaddr, port));
    }

    @Override
    public void close() {
        futureManager.close();
    }
    
    @Override
    public Localhost getLocalhost() {
        return getRouteTable().getLocalhost();
    }
    
    @Override
    public DHTFuture<PingEntity> ping(InetAddress address, 
            int port, PingConfig config) {
        return ping(new InetSocketAddress(address, port), config);
    }
    
    @Override
    public DHTFuture<PingEntity> ping(String address, 
            int port, PingConfig config) {
        return ping(new InetSocketAddress(address, port), config);
    }
    
    @Override
    public <V> DHTFuture<V> submit(DHTProcess<V> process, Config config) {
        ExecutorKey executorKey = config.getExecutorKey();
        long timeout = config.getOperationTimeoutInMillis();
        return submit(executorKey, process, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public <V> DHTFuture<V> submit(ExecutorKey executorKey,
            DHTProcess<V> process, long timeout, TimeUnit unit) {
        return futureManager.submit(executorKey, process, timeout, unit);
    }
    
    @Override
    public String toString() {
        return getLocalhost().toString();
    }
}