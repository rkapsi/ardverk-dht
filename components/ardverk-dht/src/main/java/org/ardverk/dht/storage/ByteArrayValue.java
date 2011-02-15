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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.ardverk.coding.CodingUtils;
import org.ardverk.lang.Bytes;

/**
 * An implementation of {@link Value} for {@code byte[])s.
 */
public class ByteArrayValue extends AbstractValue {

    public static final Value EMPTY = new ByteArrayValue(Bytes.EMPTY);
    
    private final byte[] value;
    
    public ByteArrayValue(byte[] value) {
        this.value = value;
    }
    
    @Override
    public byte[] getContentAsBytes() {
        return value;
    }

    @Override
    public long getContentLength() {
        return value.length;
    }

    @Override
    public InputStream getContent() {
        return new ByteArrayInputStream(value);
    }
    
    @Override
    public String toString() {
        return CodingUtils.encodeBase16(value);
    }
}