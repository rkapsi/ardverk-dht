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
 * The {@link TransportCallback} provides two interfaces to handle inbound
 * and outbound {@link Message}s.
 */
public class TransportCallback {

    private TransportCallback() {}
    
    /**
     * A callback for inbound {@link Message}s.
     */
    public static interface Inbound {
        
        /**
         * Called by {@link Transport} for every message that's being received.
         */
        public void messageReceived(Endpoint endpoint, Message message) throws IOException;
    }
    
    /**
     * A callback for outbound {@link Message}s.
     */
    public static interface Outbound {
        
        /**
         * Called for every sent {@link Message}.
         */
        public void messageSent(Message message);
        
        /**
         * Called for every message that failed to be sent.
         */
        public void handleException(Message message, Throwable t);
    }
}