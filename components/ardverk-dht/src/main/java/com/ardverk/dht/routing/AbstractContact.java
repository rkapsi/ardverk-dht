/*
 * Copyright 2009-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ardverk.dht.routing;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;
import com.ardverk.dht.lang.Identifier;

abstract class AbstractContact implements Contact {
    
    private static final long serialVersionUID = 9018341814707545676L;
    
    protected final KUID contactId;
    
    private volatile long rtt = -1L;
    
    public AbstractContact(Identifier identifier) {
        this(identifier, -1L, TimeUnit.MILLISECONDS);
    }
    
    public AbstractContact(Identifier identifier, long rtt, TimeUnit unit) {
        this.contactId = identifier.getId();
        this.rtt = unit.toMillis(rtt);
    }
    
    @Override
    public KUID getId() {
        return contactId;
    }
    
    @Override
    public boolean isType(Type type) {
        return type == getType();
    }
    
    @Override
    public boolean isAuthoritative() {
        return isType(Type.AUTHORITATIVE);
    }
    
    @Override
    public boolean isSolicited() {
        return isType(Type.SOLICITED);
    }
    
    @Override
    public boolean isUnsolicited() {
        return isType(Type.UNSOLICITED);
    }
    
    @Override
    public boolean isActive() {
        return getType().isActive();
    }
    
    @Override
    public long getRoundTripTime(TimeUnit unit) {
        return unit.convert(rtt, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public long getRoundTripTimeInMillis() {
        return getRoundTripTime(TimeUnit.MILLISECONDS);
    }
    
    @Override
    public void setRoundTripTime(long rtt, TimeUnit unit) {
        this.rtt = unit.toMillis(rtt);
    }
    
    @Override
    public long getTimeSinceLastContact(TimeUnit unit) {
        return getTimeStamp().getTime(unit);
    }
    
    @Override
    public long getTimeSinceLastContactInMillis() {
        return getTimeSinceLastContact(TimeUnit.MILLISECONDS);
    }
    
    @Override
    public boolean isTimeout(long timeout, TimeUnit unit) {
        return getTimeSinceLastContact(unit) >= timeout;
    }
    
    @Override
    public int hashCode() {
        return contactId.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Contact)) {
            return false;
        }
        
        Contact other = (Contact)o;
        return contactId.equals(other.getId());
    }
    
    @Override
    public int compareTo(Contact o) {
        return contactId.compareTo(o.getId());
    }
    
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        
        buffer.append("Type=").append(getType())
            .append(", contactId=").append(getId())
            .append(", instanceId=").append(getInstanceId())
            .append(", socketAddress=").append(getSocketAddress())
            .append(", contactAddress=").append(getContactAddress())
            .append(", rtt=").append(getRoundTripTimeInMillis());
        return buffer.toString();
    }
}
