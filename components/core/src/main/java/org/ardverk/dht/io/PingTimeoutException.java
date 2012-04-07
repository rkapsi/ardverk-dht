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

package org.ardverk.dht.io;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.dht.KUID;
import org.ardverk.dht.lang.DHTException;
import org.ardverk.dht.message.MessageType;
import org.ardverk.dht.routing.Contact;


/**
 * The {@link PingTimeoutException} is thrown if a timeout occurs.
 */
public class PingTimeoutException extends DHTException {
  
  private static final long serialVersionUID = -7330783412590437071L;

  private final RequestEntity entity;
  
  public PingTimeoutException(RequestEntity entity, 
      long time, TimeUnit unit) {
    super (time, unit);
    this.entity = entity;
  }
  
  /**
   * Returns the {@link RequestEntity}.
   */
  public RequestEntity getRequestEntity() {
    return entity;
  }
  
  /**
   * Returns the remote {@link Contact}'s {@link KUID} or {@code null}
   * if the {@link MessageType#PING} was sent to an IP-address and no
   * {@link KUID} was provided.
   */
  public KUID getContactId() {
    return entity.getId();
  }
  
  /**
   * Returns the remote {@link Contact}'s IP-address.
   */
  public SocketAddress getAddress() {
    return entity.getAddress();
  }
}