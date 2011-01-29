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

package org.ardverk.dht.message;

import java.net.SocketAddress;

import org.ardverk.lang.Arguments;

/**
 * An abstract implementation of {@link MessageFactory}.
 */
public abstract class AbstractMessageFactory implements MessageFactory {
    
    private final int length;
    
    public AbstractMessageFactory(int length) {
        this.length = Arguments.notNegative(length, "length");
    }
    
    @Override
    public MessageId createMessageId(SocketAddress dst) {
        return MessageId.createRandom(length);
    }

    @Override
    public boolean isFor(MessageId messageId, SocketAddress src) {
        return true;
    }
}