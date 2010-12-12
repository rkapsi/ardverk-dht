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

package com.ardverk.dht.io;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;
import com.ardverk.dht.lang.ArdverkException;

/**
 * 
 */
public class PingTimeoutException extends ArdverkException {
    
    private static final long serialVersionUID = -7330783412590437071L;

    private final RequestEntity entity;
    
    public PingTimeoutException(RequestEntity entity, 
            long time, TimeUnit unit) {
        super (time, unit);
        this.entity = entity;
    }
    
    /**
     * 
     */
    public RequestEntity getRequestEntity() {
        return entity;
    }
    
    public KUID getContactId() {
        return entity.getContactId();
    }
    
    public SocketAddress getAddress() {
        return entity.getAddress();
    }
}