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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Provider;

import org.ardverk.collection.Iterators;
import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.dht.config.StoreConfig;
import org.ardverk.dht.entity.StoreEntity;
import org.ardverk.dht.message.MessageFactory;
import org.ardverk.dht.message.MessageType;
import org.ardverk.dht.message.ResponseMessage;
import org.ardverk.dht.message.StoreRequest;
import org.ardverk.dht.message.StoreResponse;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.lang.TimeStamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The {@link StoreResponseHandler} manages the {@link MessageType#STORE} process.
 */
public class StoreResponseHandler extends AbstractResponseHandler<StoreEntity> {
  
  private static final Logger LOG 
    = LoggerFactory.getLogger(StoreResponseHandler.class);
  
  private final ProcessCounter counter;
  
  private final List<StoreResponse> responses = new ArrayList<>();

  private final TimeStamp creationTime = TimeStamp.now();
  
  private final Contact[] contacts;
  
  private final Iterator<Contact> it;
  
  private final Key key;
  
  private final Value value;
  
  private final StoreConfig config;
  
  private final int w;
  
  public StoreResponseHandler(
      Provider<MessageDispatcher> messageDispatcher, 
      Contact[] contacts, int k,
      Key key, Value value, 
      StoreConfig config) {
    super(messageDispatcher);
    
    this.contacts = contacts;
    this.it = Iterators.iterator(contacts);
    
    this.key = key;
    this.value = value;
    this.config = config;
    
    counter = new ProcessCounter(config.getS());
    
    int w = config.getW();
    
    int replicate = Math.min(w, k);
    if (replicate != w && LOG.isWarnEnabled()) {
      LOG.warn("replicate=" + replicate + ", w=" + w);
    }
    
    this.w = replicate;
  }

  @Override
  protected void go(AsyncFuture<StoreEntity> future) throws Exception {
    process(0);
  }

  private synchronized void process(int pop) throws IOException {
    try {
      preProcess(pop);
      
      while (counter.hasNext() && counter.getCount() < w) {
        if (!it.hasNext()) {
          break;
        }
        
        Contact dst = it.next();
        store(dst);
        
        counter.increment();
      }
      
    } finally {
      postProcess();
    }
  }
  
  private synchronized void preProcess(int pop) {
    while (0 < pop--) {
      counter.decrement();
    }
  }
  
  private synchronized void postProcess() {
    if (!counter.hasActive()) {
      long time = creationTime.getAgeInMillis();
      
      StoreResponse[] values = responses.toArray(new StoreResponse[0]);
      if (values.length == 0) {
        setException(new StoreException(key, 
            value, time, TimeUnit.MILLISECONDS));
      } else {
        setValue(new StoreEntity(contacts, key, value, 
            values, time, TimeUnit.MILLISECONDS));
      }
    }
  }
  
  private synchronized void store(Contact dst) throws IOException {
    MessageFactory factory = getMessageFactory();
    StoreRequest request = factory.createStoreRequest(dst, key, value);
    
    long defaultTimeout = config.getStoreTimeoutInMillis();
    long adaptiveTimeout = config.getAdaptiveTimeout(
        dst, defaultTimeout, TimeUnit.MILLISECONDS);
    
    send(dst, request, adaptiveTimeout, TimeUnit.MILLISECONDS);
  }
  
  @Override
  protected synchronized void processResponse(RequestEntity entity, 
      ResponseMessage response, long time, TimeUnit unit) throws IOException {
    StoreResponse message = (StoreResponse)response;
    
    try {
      responses.add(message);
    } finally {
      process(1);
    }
  }

  @Override
  protected synchronized void processTimeout(RequestEntity entity, 
      long time, TimeUnit unit) throws IOException {
    process(1);
  }

  @Override
  protected synchronized void processIllegalResponse(RequestEntity entity,
      ResponseMessage response, long time, TimeUnit unit)
      throws IOException {
    process(1);
  }

  @Override
  protected synchronized void processException(RequestEntity entity, Throwable exception) {
    try {
      process(1);
    } catch (IOException err) {
      setException(err);
    }
  }
}