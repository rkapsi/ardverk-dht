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

/**
 * This class describes the basic Kademlia IPC operations.
 */
public enum MessageType {
  
  /**
   * See Kademlia Specification(s) for PING.
   */
  PING,
  
  /**
   * See Kademlia Specification(s) for FIND_NODE.
   */
  FIND_NODE,
  
  /**
   * See Kademlia Specification(s) for FIND_VALUE.
   */
  FIND_VALUE,
  
  /**
   * See Kademlia Specification(s) for STORE.
   */
  STORE
}