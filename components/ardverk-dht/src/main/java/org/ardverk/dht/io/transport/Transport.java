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

package org.ardverk.dht.io.transport;

import java.io.IOException;
import java.net.SocketAddress;

import org.ardverk.dht.message.Message;
import org.ardverk.io.Bindable;

/**
 * The {@link Transport} provides a generic interface for the DHT to 
 * send and receive messages over UDP or any other transport layer.
 */
public interface Transport extends Bindable<TransportCallback> {

    /**
     * Returns the local {@link SocketAddress}
     */
    public SocketAddress getSocketAddress();
    
    /**
     * Sends the given {@link Message}.
     */
    public void send(Message message) throws IOException;
}