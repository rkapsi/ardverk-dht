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

import java.io.IOException;
import java.io.InputStream;

import org.ardverk.dht.message.Message;
import org.ardverk.io.Streamable;

/**
 * The {@link Message}'s value.
 * 
 * @see Message#getValue()
 */
public interface Value extends Streamable {
    
    /**
     * Returns the length of the value in {@code byte}s.
     */
    public long getContentLength();
    
    /**
     * Returns the {@link Value}'s data as an {@link InputStream}.
     * 
     * NOTE: The caller (you) is responsible for closing the {@link InputStream}.
     */
    public InputStream getContent() throws IOException;
    
    /**
     * Returns {@code true} if the {@link Value} capable of producing 
     * its data more than once.
     */
    public boolean isRepeatable();
    
    /**
     * Returns {@code true} if the {@link Value} is streaming.
     */
    public boolean isStreaming();
}