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

import org.ardverk.dht.message.MessageFactory;
import org.ardverk.lang.Arguments;


/**
 * An abstract base class for different types of message handlers.
 */
abstract class AbstractMessageHandler {

    protected final MessageDispatcher messageDispatcher;
    
    public AbstractMessageHandler(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = Arguments.notNull(messageDispatcher, "messageDispatcher");
    }
    
    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }
    
    public MessageFactory getMessageFactory() {
        return messageDispatcher.getMessageFactory();
    }
}