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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.ardverk.concurrent.EventUtils;
import org.ardverk.dht.KUID;
import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.DHTValueFuture;
import org.ardverk.dht.config.PingConfig;
import org.ardverk.dht.entity.PingEntity;
import org.ardverk.lang.Precoditions;


public abstract class AbstractRouteTable implements RouteTable {
  
  private final AtomicReference<ContactPinger> pingerRef 
    = new AtomicReference<ContactPinger>();
  
  private final List<RouteTableListener> listeners 
    = new CopyOnWriteArrayList<RouteTableListener>();
  
  @Override
  public void bind(ContactPinger pinger) {
    if (!pingerRef.compareAndSet(null, pinger)) {
      throw new IllegalStateException();
    }
  }
  
  @Override
  public void unbind() {
    pingerRef.set(null);
  }
  
  @Override
  public boolean isBound() {
    return pingerRef.get() != null;
  }

  protected DHTFuture<PingEntity> ping(Contact contact, 
      PingConfig config) {
    ContactPinger pinger = pingerRef.get();
    
    DHTFuture<PingEntity> future = null;
    if (pinger != null) {
      future = pinger.ping(contact, config);
    }
    
    if (future != null) {
      return future;
    }
    
    IllegalStateException exception 
      = new IllegalStateException();
    
    return new DHTValueFuture<PingEntity>(exception);
  }
  
  @Override
  public Contact[] select(KUID contactId) {
    return select(contactId, getK());
  }
  
  @Override
  public void addRouteTableListener(RouteTableListener l) {
    listeners.add(Precoditions.notNull(l, "l"));
  }

  @Override
  public void removeRouteTableListener(RouteTableListener l) {
    listeners.remove(Precoditions.notNull(l, "l"));
  }
  
  @Override
  public RouteTableListener[] getRouteTableListeners() {
    return listeners.toArray(new RouteTableListener[0]);
  }

  protected void fireContact(final Bucket bucket, 
      final Contact existing, final Contact contact) {
    
    if (!listeners.isEmpty()) {
      Runnable event = new Runnable() {
        @Override
        public void run() {
          for (RouteTableListener l : listeners) {
            l.handleContact(bucket, existing, contact);
          }
        }
      };
      
      EventUtils.fireEvent(event);
    }
  }
  
  protected void fireBucketSplit(final Bucket bucket, 
      final Bucket left, final Bucket right) {
    
    if (!listeners.isEmpty()) {
      Runnable event = new Runnable() {
        @Override
        public void run() {
          for (RouteTableListener l : listeners) {
            l.handleBucketSplit(bucket, left, right);
          }
        }
      };
      
      EventUtils.fireEvent(event);
    }
  }
  
  protected void fireContactAdded(final Bucket bucket, final Contact contact) {
    if (!listeners.isEmpty()) {
      Runnable event = new Runnable() {
        @Override
        public void run() {
          for (RouteTableListener l : listeners) {
            l.handleContactAdded(bucket, contact);
          }
        }
      };
      
      EventUtils.fireEvent(event);
    }
  }
  
  protected void fireContactReplaced(final Bucket bucket, 
      final Contact existing, final Contact contact) {
    
    if (!listeners.isEmpty()) {
      Runnable event = new Runnable() {
        @Override
        public void run() {
          for (RouteTableListener l : listeners) {
            l.handleContactReplaced(bucket, existing, contact);
          }
        }
      };
      
      EventUtils.fireEvent(event);
    }
  }
  
  protected void fireContactChanged(final Bucket bucket, 
      final Contact existing, final Contact contact) {
    
    if (!listeners.isEmpty()) {
      Runnable event = new Runnable() {
        @Override
        public void run() {
          for (RouteTableListener l : listeners) {
            l.handleContactChanged(bucket, existing, contact);
          }
        }
      };
      
      EventUtils.fireEvent(event);
    }
  }
  
  protected void fireContactCollision(final Contact existing, final Contact contact) {
    if (!listeners.isEmpty()) {
      Runnable event = new Runnable() {
        @Override
        public void run() {
          for (RouteTableListener l : listeners) {
            l.handleContactCollision(existing, contact);
          }
        }
      };
      
      EventUtils.fireEvent(event);
    }
  }
  
  protected void fireContactRemoved(final Bucket bucket, final Contact contact) {
    if (!listeners.isEmpty()) {
      Runnable event = new Runnable() {
        @Override
        public void run() {
          for (RouteTableListener l : listeners) {
            l.handleContactRemoved(bucket, contact);
          }
        }
      };
      
      EventUtils.fireEvent(event);
    }
  }
}