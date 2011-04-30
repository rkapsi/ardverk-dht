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

package org.ardverk.dht.message;

import java.io.Closeable;
import java.io.InputStream;

import org.ardverk.coding.BencodingInputStream.ContentInputStream;
import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.DHTValueFuture;
import org.ardverk.io.CloseAwareInputStream;
import org.ardverk.io.IoUtils;

public class StreamingValue extends AbstractValue implements Closeable {

    private final long contentLength;
    
    private final InputStream in;
    
    private final DHTValueFuture<Void> future 
            = new DHTValueFuture<Void>() {
        @Override
        protected void done() {
            super.done();
            IoUtils.close(in);
        }
    };
    
    public StreamingValue(ContentInputStream in) {
        this(in.getContentLength(), in);
    }
    
    public StreamingValue(long contentLength, InputStream in) {
        in = new CloseAwareInputStream(in) {
            @Override
            protected void complete() {
                StreamingValue.this.complete();
            }
        };
        
        this.contentLength = contentLength;
        this.in = in;
        
        if (contentLength == 0L) {
            complete();
        }
    }
    
    @Override
    public DHTFuture<Void> getContentFuture() {
        return future;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public InputStream getContent() {
        return in;
    }
    
    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public boolean isStreaming() {
        return true;
    }

    @Override
    public void close() {
        IoUtils.close(in);
    }

    private void complete() {
        future.setValue(null);
    }
}