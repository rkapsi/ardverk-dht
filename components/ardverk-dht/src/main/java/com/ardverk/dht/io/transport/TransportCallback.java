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

package com.ardverk.dht.io.transport;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * A callback interface to receive messages.
 */
public interface TransportCallback {
    
    /**
     * Called by {@link Transport} for every message that's being received.
     */
    public void received(SocketAddress src, byte[] message, 
            int offset, int length) throws IOException;
}