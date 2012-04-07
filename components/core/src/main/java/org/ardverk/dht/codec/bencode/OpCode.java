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

package org.ardverk.dht.codec.bencode;

import org.ardverk.dht.lang.IntegerValue;
import org.ardverk.dht.message.Message;
import org.ardverk.dht.message.MessageType;
import org.ardverk.dht.message.NodeRequest;
import org.ardverk.dht.message.NodeResponse;
import org.ardverk.dht.message.PingRequest;
import org.ardverk.dht.message.PingResponse;
import org.ardverk.dht.message.StoreRequest;
import org.ardverk.dht.message.StoreResponse;
import org.ardverk.dht.message.ValueRequest;
import org.ardverk.dht.message.ValueResponse;
import org.ardverk.enums.PrimitiveEnum;
import org.ardverk.enums.PrimitiveEnums;

/**
 * The {@link OpCode} is the type of a {@link Message} as 
 * it's written over the wire.
 */
enum OpCode implements PrimitiveEnum.Int, IntegerValue {
    
    PING_REQUEST(0x00, MessageType.PING),
    PING_RESPONSE(0x01, MessageType.PING),
    
    FIND_NODE_REQUEST(0x02, MessageType.FIND_NODE),
    FIND_NODE_RESPONSE(0x03, MessageType.FIND_NODE),
    
    FIND_VALUE_REQUEST(0x04, MessageType.FIND_VALUE),
    FIND_VALUE_RESPONSE(0x05, MessageType.FIND_VALUE),
    
    STORE_REQUEST(0x06, MessageType.STORE),
    STORE_RESPONSE(0x07, MessageType.STORE);
    
    private final int value;
    
    private final MessageType messageType;
    
    private OpCode(int value, MessageType messageType) {
        this.value = value;
        this.messageType = messageType;
    }
    
    @Override
    public int intValue() {
        return value;
    }
    
    @Override
    public int convert() {
        return value;
    }

    /**
     * Returns the {@link MessageType}.
     */
    public MessageType getMessageType() {
        return messageType;
    }
    
    /**
     * Returns {@code true} if the {@link OpCode} is representing a request.
     */
    public boolean isRequest() {
        switch (this) {
            case PING_REQUEST:
            case FIND_NODE_REQUEST:
            case FIND_VALUE_REQUEST:
            case STORE_REQUEST:
                return true;
            default:
                return false;
        }
    }
    
    @Override
    public String toString() {
        return name() + " (" + value + ", " + messageType + ")";
    }
    
    /**
     * Returns an {@link OpCode} for the given {@code int} value.
     */
    public static OpCode valueOf(int value) {
        return PrimitiveEnums.Int.valueOf(OpCode.class, value);
    }
    
    /**
     * Returns an {@link OpCode} for the given {@link Message}.
     */
    public static OpCode valueOf(Message message) {
        if (message instanceof PingRequest) {
            return PING_REQUEST;
        } else if (message instanceof PingResponse) {
            return PING_RESPONSE;
        } else if (message instanceof NodeRequest) {
            return FIND_NODE_REQUEST;
        } else if (message instanceof NodeResponse) {
            return FIND_NODE_RESPONSE;
        } else if (message instanceof ValueRequest) {
            return FIND_VALUE_REQUEST;
        } else if (message instanceof ValueResponse) {
            return FIND_VALUE_RESPONSE;
        } else if (message instanceof StoreRequest) {
            return STORE_REQUEST;
        } else if (message instanceof StoreResponse) {
            return STORE_RESPONSE;
        }
        
        throw new IllegalArgumentException("message=" + message);
    }
}