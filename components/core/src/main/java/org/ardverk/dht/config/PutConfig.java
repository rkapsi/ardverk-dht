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

import org.ardverk.dht.concurrent.ExecutorKey;


public class PutConfig extends Config {

  private volatile NodeConfig lookupConfig = new NodeConfig();
  
  private volatile StoreConfig storeConfig = new StoreConfig();
  
  private volatile ValueConfig getConfig = new ValueConfig();
  
  @Override
  public void setExecutorKey(ExecutorKey executorKey) {
    super.setExecutorKey(executorKey);
    lookupConfig.setExecutorKey(executorKey);
    storeConfig.setExecutorKey(executorKey);
    getConfig.setExecutorKey(executorKey);
  }
  
  public NodeConfig getLookupConfig() {
    return lookupConfig;
  }
  
  public void setLookupConfig(NodeConfig lookupConfig) {
    this.lookupConfig = lookupConfig;
  }
  
  public StoreConfig getStoreConfig() {
    return storeConfig;
  }
  
  public void setStoreConfig(StoreConfig storeConfig) {
    this.storeConfig = storeConfig;
  }
  
  public ValueConfig getGetConfig() {
    return getConfig;
  }

  public void setGetConfig(ValueConfig getConfig) {
    this.getConfig = getConfig;
  }

  public void setOperationTimeout(long timeout, TimeUnit unit) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getOperationTimeout(TimeUnit unit) {
    return ConfigUtils.getOperationTimeout(new Config[] { lookupConfig, storeConfig }, unit);
  }
}