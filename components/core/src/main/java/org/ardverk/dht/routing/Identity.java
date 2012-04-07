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

package org.ardverk.dht.routing;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.dht.KUID;
import org.ardverk.lang.TimeStamp;


public class Identity extends AbstractContact {
  
  private static final long serialVersionUID = 1919885060478043754L;

  private final TimeStamp creationTime = TimeStamp.now();
  
  private volatile boolean hidden = false;
  
  private volatile int instanceId = 0;
  
  private volatile SocketAddress socketAddress;
  
  private volatile SocketAddress contactAddress;
  
  public Identity(int keySize, SocketAddress contactAddress) {
    this(KUID.createRandom(keySize), contactAddress);
  }
  
  public Identity(KUID contactId, SocketAddress contactAddress) {
    super(contactId);
    this.contactAddress = contactAddress;
  }
  
  @Override
  public TimeStamp getCreationTime() {
    return creationTime;
  }

  @Override
  public TimeStamp getTimeStamp() {
    return TimeStamp.now();
  }
  
  @Override
  public Type getType() {
    return Type.AUTHORITATIVE;
  }

  @Override
  public int getInstanceId() {
    return instanceId;
  }
  
  /**
   * Sets the instance ID to the given number.
   */
  public void setInstanceId(int instanceId) {
    this.instanceId = instanceId;
  }

  @Override
  public boolean isHidden() {
    return hidden;
  }

  /**
   * Sets weather or not this instance is invisible to
   * other nodes in the network.
   */
  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }

  /**
   * Returns the {@link Identity}'s {@link SocketAddress} as seen
   * by other {@link Contact}s in the network.
   */
  @Override
  public SocketAddress getSocketAddress() {
    return socketAddress;
  }
  
  /**
   * Sets the {@link Identity}'s {@link SocketAddress} as seen
   * by other {@link Contact}s in the network.
   */
  public void setSocketAddress(SocketAddress socketAddress) {
    this.socketAddress = socketAddress;
  }
  
  @Override
  public SocketAddress getContactAddress() {
    return contactAddress;
  }
  
  public void setContactAddress(SocketAddress contactAddress) {
    this.contactAddress = contactAddress;
  }

  @Override
  public SocketAddress getRemoteAddress() {
    return getContactAddress();
  }

  @Override
  public long getRoundTripTime(TimeUnit unit) {
    return -1L;
  }
  
  @Override
  public void setRoundTripTime(long rtt, TimeUnit unit) {
    // Do nothing, a localhost cannot have a RTT
  }

  @Override
  public Contact merge(Contact other) {
    throw new UnsupportedOperationException();
  }
}