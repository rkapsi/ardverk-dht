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

package org.ardverk.dht.rsrc;

import java.util.Map;

/**
 * An abstract implementation of {@link Key}.
 */
public abstract class AbstractKey implements Key {

  @Override
  public String getPath() {
    return getURI().getPath();
  }
  
  @Override
  public Map<String, String> getQueryString() {
    return KeyUtils.getQueryString(getURI());
  }
  
  @Override
  public int compareTo(Key o) {
    return getPath().compareTo(o.getPath());
  }

  @Override
  public int hashCode() {
    return getPath().hashCode();
  }
  
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof Key)) {
      return false;
    }
    
    return compareTo((Key)o) == 0;
  }
  
  @Override
  public String toString() {
    return getURI().toString();
  }
}