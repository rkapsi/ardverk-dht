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

package org.ardverk.dht;

import org.ardverk.dht.concurrent.ArdverkFuture;
import org.ardverk.dht.config.QuickenConfig;
import org.ardverk.dht.entity.QuickenEntity;
import org.ardverk.dht.routing.RouteTable;

/**
 * The {@link QuickenService} provides methods to keep the DHT fresh.
 */
interface QuickenService {

    /**
     * Performs specially targeted PING and FIND_NODE requests with the
     * goal to refresh the {@link RouteTable} and keep it up-to-date.
     */
    public ArdverkFuture<QuickenEntity> quicken(QuickenConfig config);
}