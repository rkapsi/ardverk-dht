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

package org.ardverk.dht.io;

import org.ardverk.dht.KUID;
import org.ardverk.dht.message.Message;

/**
 * The {@link MessageListener} can be used to listen for all incoming
 * and outgoing {@link Message}s.
 */
public interface MessageListener {
    
    /**
     * Called for every {@link Message} that was sent.
     */
    public void handleMessageSent(KUID contactId, Message message);
    
    /**
     * Called for every {@link Message} that was received.
     */
    public void handleMessageReceived(Message message);
}