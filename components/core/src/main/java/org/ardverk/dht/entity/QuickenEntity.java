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

package org.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import org.ardverk.dht.concurrent.DHTFuture;

/**
 * A default implementation of {@link QuickenEntity}.
 */
public class QuickenEntity extends Entity {

  private final DHTFuture<PingEntity>[] pingFutures;
  
  private final DHTFuture<NodeEntity>[] lookupFutures;
  
  public QuickenEntity(DHTFuture<PingEntity>[] pingFutures, 
      DHTFuture<NodeEntity>[] lookupFutures, long time, TimeUnit unit) {
    super(time, unit);
    
    this.pingFutures = pingFutures;
    this.lookupFutures = lookupFutures;
  }

  public DHTFuture<PingEntity>[] getPingFutures() {
    return pingFutures;
  }

  public DHTFuture<NodeEntity>[] getLookupFutures() {
    return lookupFutures;
  }
}