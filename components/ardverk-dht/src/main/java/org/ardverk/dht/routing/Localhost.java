/*
 * Copyright 2009-2011 Roger Kapsi
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
import org.ardverk.dht.io.transport.Transport;
import org.ardverk.io.Bindable;
import org.ardverk.lang.TimeStamp;


public class Localhost extends AbstractContact implements Bindable<Transport> {
    
    private static final long serialVersionUID = 1919885060478043754L;

    private final TimeStamp creationTime = TimeStamp.now();
    
    private volatile boolean invisible = false;
    
    private volatile int instanceId = 0;
    
    private volatile Transport transport;
    
    private volatile SocketAddress contactAddress;
    
    public Localhost(int keySize) {
        this(KUID.createRandom(keySize));
    }
    
    public Localhost(KUID contactId) {
        super(contactId);
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
    public boolean isInvisible() {
        return invisible;
    }

    /**
     * Sets weather or not this instance is invisible to
     * other nodes in the network.
     */
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }
    
    /**
     * Binds the {@link Localhost} to the given {@link Transport}.
     */
    @Override
    public synchronized void bind(Transport transport) {
        this.transport = transport;
        
        SocketAddress bindaddr = transport.getSocketAddress();
        this.contactAddress = bindaddr;
    }
    
    /**
     * Unbinds the {@link Localhost} from a {@link Transport}.
     */
    @Override
    public synchronized void unbind() {
        this.transport = null;
        this.contactAddress = null;
    }

    /**
     * Returns true if the {@link Localhost} is bound to a {@link Transport}.
     */
    @Override
    public synchronized boolean isBound() {
        return transport != null;
    }

    @Override
    public SocketAddress getSocketAddress() {
        Transport transport = this.transport;
        return transport != null ? transport.getSocketAddress() : null;
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