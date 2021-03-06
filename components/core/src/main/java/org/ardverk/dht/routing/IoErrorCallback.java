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

import org.ardverk.dht.KUID;


/**
 * A callback interface for the {@link RouteTable}.
 */
interface IoErrorCallback {
  
  /**
   * A callback that is called if an I/O error occurred for the given
   * {@link KUID} and {@link SocketAddress} pair.
   */
  public void handleIoError(KUID contactId, SocketAddress address);
}