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

package com.ardverk.dht.message;

import java.net.SocketAddress;
import java.util.Random;

import org.ardverk.lang.Arguments;

public abstract class AbstractMessageFactory implements MessageFactory {
    
    private static final Random GENERATOR = new Random();
    
    private final Random generator;
    
    private final int length;
    
    public AbstractMessageFactory(int length) {
        this(GENERATOR, length);
    }
    
    public AbstractMessageFactory(Random generator, int length) {
        this.generator = Arguments.notNull(generator, "generator");
        this.length = Arguments.notNegative(length, "length");
    }
    
    @Override
    public MessageId createMessageId(SocketAddress dst) {
        byte[] messageId = new byte[length];
        generator.nextBytes(messageId);
        return MessageId.create(messageId);
    }

    @Override
    public boolean isFor(MessageId messageId, SocketAddress src) {
        return true;
    }
}