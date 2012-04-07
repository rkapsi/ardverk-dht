/*
 * Copyright 2009-2012 Roger Kapsi
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

import java.util.Random;

import org.ardverk.coding.CodingUtils;
import org.ardverk.dht.security.SecurityUtils;
import org.ardverk.lang.ByteArray;

/**
 * A {@link MessageId} is a randomly generated identifier for {@link Message}s.
 */
public final class MessageId extends ByteArray<MessageId> implements Cloneable {

    private static final long serialVersionUID = 6653397095695641792L;
    
    private static final Random GENERATOR = SecurityUtils.createRandom();
    
    /**
     * Creates and returns a random {@link MessageId} of the given length.
     */
    public static MessageId createRandom(int length) {
        byte[] messageId = new byte[length];
        GENERATOR.nextBytes(messageId);
        return new MessageId(messageId);
    }
    
    /**
     * Creates and returns a random {@link MessageId} of the same length 
     * as the given {@link MessageId}.
     */
    public static MessageId createRandom(MessageId otherId) {
        return createRandom(otherId.length());
    }
    
    /**
     * Creates and returns a {@link MessageId} from the given {@code byte[]}.
     */
    public static MessageId create(byte[] messageId) {
        return new MessageId(messageId);
    }
    
    /**
     * Creates and returns a {@link MessageId} from the given {@code byte[]}.
     */
    public static MessageId create(byte[] messageId, int offset, int length) {
        byte[] copy = new byte[length];
        System.arraycopy(messageId, 0, copy, 0, copy.length);
        return new MessageId(copy);
    }
    
    /**
     * Creates and returns a {@link MessageId} from the Base-16 (hex)
     * encoded {@link String}.
     */
    public static MessageId create(String messageId) {
        return create(CodingUtils.decodeBase16(messageId));
    }
    
    private MessageId(byte[] messageId) {
        super(messageId);
    }
    
    /**
     * Returns {@code true} if the given {@link MessageId} is 
     * compatible with this {@link MessageId}.
     */
    public boolean isCompatible(MessageId otherId) {
        return otherId != null && length() == otherId.length();
    }
    
    @Override
    public MessageId clone() {
        return this;
    }
    
    @Override
    public String toString() {
        return toHexString();
    }
}