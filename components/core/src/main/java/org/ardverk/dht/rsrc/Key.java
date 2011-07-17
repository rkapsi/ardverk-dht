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

package org.ardverk.dht.rsrc;

import java.net.URI;
import java.util.Map;

import org.ardverk.dht.lang.Identifier;


/**
 * A {@link Key} is an unique identifier of a {@link Value}.
 */
public interface Key extends Comparable<Key>, Identifier {
    
    /**
     * 
     */
    public String getBucket();
    
    /**
     * 
     */
    public Key strip();
    
    /**
     * Returns the resource's {@link URI}.
     */
    public URI getURI();
    
    /**
     * Returns the {@link URI}'s path
     * 
     * @see URI#getPath()
     */
    public String getPath();
    
    /**
     * Returns the {@link URI}'s query string
     */
    public Map<String, String> getQueryString();
}