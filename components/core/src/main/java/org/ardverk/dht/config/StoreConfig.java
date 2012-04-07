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

package org.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

import org.ardverk.dht.routing.RouteTableConfig;
import org.ardverk.utils.TimeUtils;

public class StoreConfig extends Config {
  
  private static final long DEFAULT_STORE_TIMEOUT
    = TimeUtils.convert(15L, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
  
  private volatile int s = 5;
  
  private volatile int w = RouteTableConfig.DEFAULT_K;
  
  public StoreConfig() {
    super(DEFAULT_STORE_TIMEOUT, TimeUnit.MILLISECONDS);
  }
  
  public StoreConfig(long storeTimeout, TimeUnit unit) {
    super(storeTimeout, unit);
  }

  public long getStoreTimeout(TimeUnit unit) {
    return getOperationTimeout(unit);
  }

  public long getStoreTimeoutInMillis() {
    return getOperationTimeoutInMillis();
  }

  public void setStoreTimeout(long timeout, TimeUnit unit) {
    setOperationTimeout(timeout, unit);
  }

  public int getS() {
    return s;
  }

  public void setS(int s) {
    this.s = s;
  }

  public int getW() {
    return w;
  }

  public void setW(int w) {
    this.w = w;
  }
}