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

import java.io.IOException;
import java.io.InputStream;

import org.ardverk.io.IoUtils;
import org.ardverk.io.SequenceInputStream;

public class CompositeValue extends AbstractValue {

    private final Value[] values;
    
    public CompositeValue(Value... values) {
        this.values = values;
    }

    @Override
    public long getContentLength() {
        long contentLength = 0L;
        for (Value value : values) {
            contentLength += value.getContentLength();
        }
        return contentLength;
    }

    @Override
    public InputStream getContent() throws IOException {
        InputStream[] streams = new InputStream[values.length];
        
        boolean success = false;
        try {
            for (int i = 0; i < values.length; i++) {
                streams[i] = values[i].getContent();
            }
            success = true;
        } finally {
            if (!success) {
                IoUtils.closeAll(streams);
            }
        }
        
        return new SequenceInputStream(streams);
    }

    @Override
    public boolean isRepeatable() {
        for (Value value : values) {
            if (!value.isRepeatable()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isStreaming() {
        for (Value value : values) {
            if (!value.isStreaming()) {
                return false;
            }
        }
        return true;
    }
}
