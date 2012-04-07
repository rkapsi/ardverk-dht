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

import java.util.concurrent.TimeUnit;

/**
 * An object that has an keeps track of its Round-Trip-Time (RTT).
 */
public interface RoundTripTime {

  /**
   * Returns the {@link Contact}'s Round-Trip-Time (RTT) or a negative 
   * value if the RTT is unknown.
   */
  public long getRoundTripTime(TimeUnit unit);
  
  /**
   * Returns the {@link Contact}'s Round-Trip-Time (RTT) in milliseconds
   * or a negative value if the RTT is unknown.
   */
  public long getRoundTripTimeInMillis();
  
  /**
   * Changes the {@link Contact}'s Round-Trip-Time (RTT)
   */
  public void setRoundTripTime(long rtt, TimeUnit unit);
}