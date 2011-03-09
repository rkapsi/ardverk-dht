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

import org.ardverk.dht.message.Message;

/**
 * The {@link TransportCallback} is called by {@link Transport}.
 */
public interface TransportCallback {

    /**
     * Called by {@link Transport} for every {@link Message} that's being received.
     */
    public void messageReceived(Endpoint endpoint, Message message) throws IOException;
    
    /**
     * Called by {@link Transport} for every {@link Message} that has been sent.
     */
    public void messageSent(Endpoint endpoint, Message message);
    
    /**
     * Called by {@link Transport} for every {@link Message} that failed to be sent.
     */
    public void handleException(Endpoint endpoint, Message message, Throwable t);
}
