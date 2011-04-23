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

package org.ardverk.dht.storage;

/**
 * An abstract implementation of {@link ResourceId}.
 */
public abstract class AbstractResourceId implements ResourceId {

    @Override
    public int compareTo(ResourceId o) {
        return getURI().compareTo(o.getURI());
    }

    @Override
    public int hashCode() {
        return getURI().hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ResourceId)) {
            return false;
        }
        
        ResourceId other = (ResourceId)o;
        return getURI().equals(other.getURI());
    }
    
    @Override
    public String toString() {
        return getURI().toString();
    }
}
