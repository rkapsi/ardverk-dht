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

package com.ardverk.dht.storage;

import java.io.InputStream;

import org.ardverk.io.Writable;

import com.ardverk.dht.DHT;

/**
 * A value that's stored in the {@link DHT}.
 */
public interface Value extends Writable {

    /**
     * Returns the length of the {@link Value} in {@code byte}s.
     */
    public long getContentLength();
    
    /**
     * Returns the content as an {@link InputStream}. The user is
     * responsible for closing it!
     */
    public InputStream getContent();
}
