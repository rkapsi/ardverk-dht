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
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ardverk.dht.message.Message;
import org.ardverk.dht.message.MessageFactory;
import org.ardverk.dht.message.NodeRequest;
import org.ardverk.dht.message.PingRequest;
import org.ardverk.dht.message.RequestMessage;
import org.ardverk.dht.message.ResponseMessage;
import org.ardverk.dht.message.StoreRequest;
import org.ardverk.dht.message.ValueRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A default implementation of {@link MessageDispatcher}.
 */
@Singleton
public class DefaultMessageDispatcher extends MessageDispatcher {

  private static final Logger LOG 
    = LoggerFactory.getLogger(DefaultMessageDispatcher.class);
  
  private final DefaultMessageHandler defaultHandler;
  
  private final PingRequestHandler ping;
  
  private final NodeRequestHandler node;
  
  private final ValueRequestHandler value;
  
  private final StoreRequestHandler store;
  
  @Inject
  public DefaultMessageDispatcher(MessageFactory factory,
      DefaultMessageHandler defaultHandler, PingRequestHandler ping,
      NodeRequestHandler node, ValueRequestHandler value,
      StoreRequestHandler store) {
    super(factory);
    
    this.defaultHandler = defaultHandler;
    this.ping = ping;
    this.node = node;
    this.value = value;
    this.store = store;
  }

  /**
   * Returns the {@link DefaultMessageHandler} which is called for 
   * every incoming {@link Message}.
   */
  public DefaultMessageHandler getDefaultHandler() {
    return defaultHandler;
  }
  
  /**
   * Returns the {@link PingRequestHandler} which is called for
   * every incoming {@link PingRequest}.
   */
  public PingRequestHandler getPingRequestHandler() {
    return ping;
  }

  /**
   * Returns the {@link NodeRequestHandler} which is called for every
   * incoming {@link NodeRequest}.
   */
  public NodeRequestHandler getNodeRequestHandler() {
    return node;
  }
  
  /**
   * Returns the {@link ValueRequestHandler} which is called for every
   * incoming {@link ValueRequest}.
   */
  public ValueRequestHandler getValueRequestHandler() {
    return value;
  }

  /**
   * Returns the {@link StoreRequestHandler} which is called for every
   * incoming {@link StoreRequest}.
   */
  public StoreRequestHandler getStoreRequestHandler() {
    return store;
  }

  @Override
  protected ResponseMessage handleRequest0(RequestMessage request) throws IOException {
    
    defaultHandler.handleRequest(request);
    
    if (request instanceof PingRequest) {
      return ping.handleRequest(request);
    } else if (request instanceof NodeRequest) {
      return node.handleRequest(request);
    } else if (request instanceof ValueRequest) {
      return value.handleRequest(request);
    } else if (request instanceof StoreRequest) {
      return store.handleRequest(request);
    }
    
    return unhandledRequest(request);
  }
  
  @Override
  protected boolean handleResponse(MessageCallback callback,
      RequestEntity entity, ResponseMessage response, long time,
      TimeUnit unit) throws IOException {
    
    boolean success = super.handleResponse(callback, entity, response, time, unit);
    defaultHandler.handleResponse(entity, response, time, unit);
    return success;
  }

  
  @Override
  protected void handleIllegalResponse(MessageCallback callback,
      RequestEntity entity, ResponseMessage response, long time,
      TimeUnit unit) throws IOException {
    super.handleIllegalResponse(callback, entity, response, time, unit);
    defaultHandler.handleIllegalResponse(entity, response, time, unit);
  }

  @Override
  protected void handleException(MessageCallback callback,
      RequestEntity entity, Throwable t) {
    super.handleException(callback, entity, t);
    defaultHandler.handleException(entity, t);
  }

  @Override
  protected void handleTimeout(MessageCallback callback,
      RequestEntity entity, long time, TimeUnit unit)
      throws IOException {
    
    super.handleTimeout(callback, entity, time, unit);
    defaultHandler.handleTimeout(entity, time, unit);
  }

  @Override
  protected void lateResponse(ResponseMessage response) throws IOException {
    defaultHandler.handleLateResponse(response);
  }

  protected ResponseMessage unhandledRequest(RequestMessage message) throws IOException {
    if (LOG.isErrorEnabled()) {
      LOG.error("Unhandled Request: " + message);
    }
    return null;
  }
}