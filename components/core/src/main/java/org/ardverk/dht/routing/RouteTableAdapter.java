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

public class RouteTableAdapter implements RouteTableListener {
  
  @Override
  public void handleContact(Bucket bucket, Contact existing, Contact contact) {
  }

  @Override
  public void handleBucketSplit(Bucket bucket, Bucket left, Bucket right) {
  }

  @Override
  public void handleContactAdded(Bucket bucket, Contact contact) {
  }

  @Override
  public void handleContactReplaced(Bucket bucket, Contact existing,
      Contact contact) {
  }

  @Override
  public void handleContactChanged(Bucket bucket, Contact existing,
      Contact contact) {
  }

  @Override
  public void handleContactRemoved(Bucket bucket, Contact contact) {
  }

  @Override
  public void handleContactCollision(Contact existing, Contact contact) {
  }
}